package com.microtomato.hirun.framework.cache.memcache.driver.io;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 带主备功能的 SockIO 桶
 *
 * @author Steven
 * @date 2019-10-15
 */
@Slf4j
public class HABaseSockIoBucket extends BaseSockIoBucket {

    private boolean useNio = true;

    /**
     * 主地址连接池
     */
    private LinkedBlockingQueue<ISockio> masterSocks = new LinkedBlockingQueue<>();

    /**
     * 备地址连接池
     */
    private LinkedBlockingQueue<ISockio> slaveSocks = new LinkedBlockingQueue<>();

    /**
     * 主地址
     */
    private String masterHost;

    /**
     * 主端口
     */
    private int masterPort;

    /**
     * 备地址
     */
    private String slaveHost;

    /**
     * 备端口
     */
    private int slavePort;

    /**
     * 连接池大小
     */
    private int poolSize;

    /**
     * 桶的版本号
     */
    private int version = 0;

    /**
     * 桶状态
     */
    private int stateCode = STATE_RAW;

    /**
     * 构造桶
     *
     * @param masterHost 主地址
     * @param masterPort 主端口
     * @param slaveHost  备地址
     * @param slavePort  备端口
     * @param poolSize   连接池大小
     */
    public HABaseSockIoBucket(String masterHost, int masterPort, String slaveHost, int slavePort, int poolSize, boolean useNio) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.slaveHost = slaveHost;
        this.slavePort = slavePort;
        this.poolSize = poolSize;
        this.useNio = useNio;
    }

    /**
     * 桶的初始化
     *
     * @throws IOException
     */
    @Override
    public boolean init() throws IOException {

        // 每次初始化桶递增版本号
        this.version++;

        // 初始化主地址
        for (int i = 0; i < poolSize; i++) {
            ISockio sock = null;
            if (this.useNio) {
                sock = new SockNIO(this, masterHost, masterPort, version, true);
            } else {
                sock = new SockBIO(this, masterHost, masterPort, version, true);
            }

            if (sock.init()) {
                masterSocks.add(sock);
            } else {
                // 如果连不上, 就没必要初始化第二次，不卡
                break;
            }
        }

        // 初始化备地址，默认只初始化一个连接
        {
            ISockio sock = null;
            if (this.useNio) {
                sock = new SockNIO(this, slaveHost, slavePort, version, false);
            } else {
                sock = new SockBIO(this, slaveHost, slavePort, version, false);
            }

            if (sock.init()) {
                slaveSocks.add(sock);
            }
        }

        if (masterSocks.size() == this.poolSize) {
            if (slaveSocks.size() == 1) {
                this.stateCode = STATE_OKOK;
            } else {
                this.stateCode = STATE_OKER;
            }
            return true;
        } else {
            if (slaveSocks.size() == 1) {
                // 主地址不可用的情况下，启用备地址
                this.stateCode = STATE_EROK;

                // 1. 备地址再初始化(poolSize - 1)个连接
                for (int i = 0; i < poolSize - 1; i++) {
                    ISockio sock = null;
                    if (this.useNio) {
                        sock = new SockNIO(this, slaveHost, slavePort, version, false);
                    } else {
                        sock = new SockBIO(this, slaveHost, slavePort, version, false);
                    }
                    if (sock.init()) {
                        slaveSocks.add(sock);
                    }
                }

                // 释放主地址资源
                this.closeMaster();
                return true;
            } else {
                this.stateCode = STATE_ERER;
                // 释放主地址资源
                this.close();
                return false;
            }
        }
    }

    /**
     * 桶的销毁，关闭桶中所有的连接
     */
    @Override
    public void close() {

        // 释放主地址资源
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockio sock = masterSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("memcached释放主地址连接发生异常!", e);
            }
        }

        // 释放备地址资源
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockio sock = slaveSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("memcached释放备地址连接发生异常!", e);
            }
        }

        masterSocks.clear();
        slaveSocks.clear();
    }

    /**
     * 释放主地址资源
     */
    private void closeMaster() {
        for (int i = 0; i < poolSize; i++) {
            try {
                ISockio sock = masterSocks.poll();
                if (null != sock) {
                    sock.close();
                }
            } catch (Exception e) {
                log.error("memcached心跳发生异常!", e);
            }
        }
    }

    /**
     * 借走一个socket连接
     *
     * @return
     */
    @Override
    public ISockio borrowSockio() {
        return borrowSockio(2);
    }

    /**
     * 借走一个socket连接
     *
     * @return
     */
    @Override
    public ISockio borrowSockio(long timeout) {

        ISockio sock = null;

        try {
            switch (this.stateCode) {
                case STATE_OKOK:
                    sock = masterSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
                case STATE_OKER:
                    sock = masterSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
                case STATE_EROK:
                    sock = slaveSocks.poll(timeout, TimeUnit.SECONDS);
                    break;
                default:
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return sock;

    }

    /**
     * 归还一个socket连接（有没有可能还错位置呢? 即便还错也只是链接增多，不会减少）
     *
     * @param sock
     */
    @Override
    public void returnSockio(ISockio sock) {

        if (this.version != sock.getVersion()) {
            // 连接的版本号和桶的版本号不一致，说明是前次桶资源释放不完全。
            try {
                sock.close();
            } catch (IOException e) {
                log.error("memcached释放过期连接发生异常!", e);
            }
            return;
        }

        if (sock.isMaster()) {
            masterSocks.add(sock);
        } else {
            slaveSocks.add(sock);
        }
    }

    @Override
    public boolean delSock(ISockio sock) {

        if (sock.isMaster()) {
            return masterSocks.remove(sock);
        } else {
            return slaveSocks.remove(sock);
        }

    }

    @Override
    public int healthCheck() throws IOException, InterruptedException {

        boolean masterAlive = false;
        boolean slaveAlive = false;

        // 1. 主地址健康检查
        {
            ISockio io = masterSocks.poll(1, TimeUnit.SECONDS);
            if (null != io) {
                if (io.isAlive()) {
                    masterAlive = true;
                }
                io.release(); // 记得释放
            } else {
                ISockio sock = null;
                if (this.useNio) {
                    sock = new SockNIO(this, masterHost, masterPort, -1, false);
                } else {
                    sock = new SockBIO(this, masterHost, masterPort, -1, false);
                }

                if (null != sock) {
                    if (sock.init()) {
                        if (sock.isAlive()) {
                            masterAlive = true;
                        }
                    }
                    sock.close(); // 记得释放
                }
            }
        }

        // 2. 备地址健康检查
        {
            ISockio io = slaveSocks.poll(1, TimeUnit.SECONDS);
            if (null != io) {
                if (io.isAlive()) {
                    slaveAlive = true;
                }
                io.release(); // 记得释放
            } else {
                ISockio sock = null;
                if (this.useNio) {
                    sock = new SockNIO(this, slaveHost, slavePort, -1, false);
                } else {
                    sock = new SockBIO(this, slaveHost, slavePort, -1, false);
                }


                if (sock.init()) {
                    if (sock.isAlive()) {
                        slaveAlive = true;
                    }
                    sock.close(); // 记得释放
                }
            }
        }

        if (masterAlive) {
            return slaveAlive ? STATE_OKOK : STATE_OKER;
        } else {
            return slaveAlive ? STATE_EROK : STATE_ERER;
        }
    }

    @Override
    public int getStateCode() {
        return this.stateCode;
    }

    @Override
    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    @Override
    public String getAddress() {
        return masterHost + ":" + masterPort;
    }

    @Override
    public int compareTo(BaseSockIoBucket o) {
        return getAddress().compareTo(o.getAddress());
    }
}
