package com.microtomato.hirun.framework.cache.memcache.driver.io;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 基于 OIO 实现
 *
 * @author Steven
 * @date 2019-10-15
 */
@Slf4j
public class SockBIO implements ISockio {

	private static final int BUFFED_INITSIZE = 1024 * 64;
	
	private BaseSockIoBucket bucket;
	private String host;
	private int port;
	private int timeout = 5000;
	private boolean tcpNoDelay = true;
	
	private Socket socket;
	
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private ByteArrayOutputStream baos;

	private boolean isMaster;
	private int version = 0;
	
	/**
	 * SockOIO构造函数
	 * 
	 * @param bucket 归属桶
	 * @param host 缓存服务器地址
	 * @param port 缓存服务器端口
	 * @param version
	 * @param isMaster
	 * @throws IOException
	 */
	public SockBIO(BaseSockIoBucket bucket, String host, int port, int version, boolean isMaster) {
		this.bucket = bucket;
		this.host = host;
		this.port = port;
		this.version = version;
		this.isMaster = isMaster;
	}

	/**
	 * 连接初始化
	 * 
	 * @throws IOException
	 */
	@Override
	public boolean init() {
		try {
			InetAddress addr = InetAddress.getByName(host);
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(addr, port), 3000); // 连接3秒超时

			if (timeout > 0) {
				this.socket.setSoTimeout(timeout);
			}

			this.socket.setTcpNoDelay(tcpNoDelay);
			this.in = new BufferedInputStream(socket.getInputStream(), BUFFED_INITSIZE); 
			this.out = new BufferedOutputStream(socket.getOutputStream(), BUFFED_INITSIZE);
			this.baos = new ByteArrayOutputStream(300);
			return true;
		} catch (IOException e) {
			log.error("memcached socket连接建立失败" + host + ":" + port + " " + e);
		}
		
		return false;
	}
	
	/**
	 * 向套接字中写入一个byte数组
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	@Override
	public void write(byte[] bytes) throws IOException {
		out.write(bytes);
	}

	/**
	 * 
	 * @param b
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {		
		return in.read(b, off, len);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	@Override
	public void flush() throws IOException {
		out.flush();
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public boolean isConnected() {
		return (socket != null && socket.isConnected());
	}

	/**
	 * memcached连接的心跳检查
	 * 
	 * @return
	 */
	@Override
	public boolean isAlive() {

		if (!isConnected()) {
			return false;
		}
		
		try {
			this.write("version\r\n".getBytes());
			this.flush();
			byte[] rtn = readLineBytes();
			if (null == rtn) {
				return false;
			}
			
			return new String(rtn).startsWith("VERSION");
		} catch (IOException e) {
			log.error("心跳检测异常！", e);
			return false;
		}
	}

	/**
	 * 关闭
	 */
	@Override
	public void close() throws IOException {

		if (null != in) {
			in.close();
			in = null;
		}

		if (null != out) {
			out.close();
			out = null;
		}

		if (null != socket) {
			socket.close();
			socket = null;
		}

		bucket.delSock(this);
	}

	/**
	 * 读取一行，返回byte数组。
	 * 
	 * @return
	 * @throws IOException
	 */
	@Override
	public byte[] readLineBytes() throws IOException {

		byte[] rtn = null;

		boolean eol = false;
		byte[] b = new byte[1];
		while (in.read(b, 0, 1) != -1) {
			if (13 == b[0]) {
				eol = true;
				continue;
			} else {
				if ((eol) && (10 == b[0])) {
					break;
				}
				eol = false;
			}

			baos.write(b, 0, 1);
		}

		if (null == baos || baos.size() <= 0) {
			return null;
		}

		rtn = baos.toByteArray();
		baos.reset(); // 重置
		return rtn;
	}

	/**
	 * 释放连接
	 */
	@Override
	public void release() {
		this.bucket.returnSockio(this);
	}
	
	/**
	 * 获得连接所归属的桶
	 */
	@Override
	public BaseSockIoBucket getBucket() {
		return bucket;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public boolean isMaster() {
		return isMaster;
	}

}
