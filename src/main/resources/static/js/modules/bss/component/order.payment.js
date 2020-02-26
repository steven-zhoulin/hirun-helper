define(['vue','ELEMENT','ajax', 'vxe-table', 'vueselect', 'util'], function(Vue,element,ajax, table, vueSelect, util){
    Vue.use(table);
    Vue.component('order-payment', {
        data : function(){
            return {
                payItems: [],
                payItem:{},
                payForm:{
                    needPay: null,
                    payDate: util.getNowDate()
                },
                periodDisabled: true,
                payments: [],
                payItemOptions:[],
                dialogVisible: false,
                validRules: {
                    money: [
                        {type:'number', message: '金额必须为数字'},
                        {pattern:/^[0-9]+(\.\d+)?$/, message: '金额必须为正数'}
                    ]
                },
                checkPay: (rule, value, callback) => {
                    if (!value) {
                        callback(new Error('请输入收款金额'));
                    }
                    if (!/^[0-9]+(\.\d+)?$/.test(value)) {
                        callback(new Error('金额必须为正数'));
                    }
                    callback();
                }
            }
        },

        template: `
            <div>
                <el-form ref="payForm" :model="payForm" label-width="80px">
                <el-row :gutter="12">
                    <el-col :span="12">
                        <el-form-item label="收款日期" 
                                    prop="payDate"
                                    :rules="[
                                        { required: true, message: '请选择收款日期', trigger: 'blur' }
                                    ]">
                            <el-date-picker
                                    type="date"
                                    v-model="payForm.payDate"
                                    value-format="yyyy-MM-dd"
                                    placeholder="选择日期"
                                    style="width:100%"
                                    >
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="收款金额" 
                            prop="needPay"
                            :rules="[
                                { required: true, message: '请输入收款金额', trigger: 'blur' },
                                { validator: checkPay, trigger: ['blur', 'change']}
                            ]">
                            <el-input v-model="payForm.needPay" style="width:100%">
                                <template slot="append">元</template>
                            </el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                </el-form>
                <el-divider><i class="el-icon-money"></i>付款项目</el-divider>
                <el-row style="margin-bottom: 18px;">
                    <el-button type="primary" @click="popupDialog">新增收款项</el-button>
                    <el-dialog title="新增收款项" :visible.sync="dialogVisible">
                        <el-form :inline="false" :model="payItem" label-position="right" label-width="80px">
                            <el-row>
                                <el-col :span="24">
                                    <el-form-item label="收款项目">
                                        <el-cascader
                                            v-model="payItem.selectedPayItem"
                                            placeholder="搜索收款项目"
                                            :props="{ multiple: true }"
                                            :options="payItemOptions"
                                            @change="handlePayItem"
                                            clearable
                                            filterable
                                            style="width:100%">
                                        </el-cascader>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </el-form>
                        <div slot="footer" class="dialog-footer">
                            <el-button type="primary" @click="confirmSelectPayItem">确 定</el-button>
                        </div>
                    </el-dialog>
                </el-row>
                <vxe-table
                    border
                    resizable
                    ref="payItem"
                    show-footer
                    show-overflow
                    highlight-hover-row
                    :data="payItems"
                    :edit-rules="validRules"
                    :footer-method="footerMethod"
                    :edit-config="{trigger: 'click', mode: 'cell'}">
                    <vxe-table-column field="payItemName" title="款项"></vxe-table-column>
                    <vxe-table-column field="periodName" title="期数" width="100"></vxe-table-column>
                    <vxe-table-column field="money" title="应付金额（单位：元）" width="180" :edit-render="{name: 'input', attrs: {type: 'number'}}"></vxe-table-column>
                    <vxe-table-column title="操作" width="100" show-overflow>
                        <template v-slot="{ row }">
                            <el-button
                                size="mini"
                                type="danger"
                                icon="el-icon-delete"
                                @click="deletePayItem(row)">删除</el-button>
                        </template>
                    </vxe-table-column>
                </vxe-table>
                <el-divider><i class="el-icon-money"></i>付款方式</el-divider>
                <vxe-table
                    border
                    resizable
                    ref="payMoney"
                    show-footer
                    show-overflow
                    highlight-hover-row
                    :data="payments"
                    :edit-rules="validRules"
                    :footer-method="footerMethod"
                    :edit-config="{trigger: 'click', mode: 'cell'}">
                    <vxe-table-column field="paymentName" title="付款方式"></vxe-table-column>
                    <vxe-table-column field="money" title="付款金额（单位：元）" :edit-render="{name: 'input', attrs: {type: 'number'}}"></vxe-table-column>
                </vxe-table>
                <br/>
            </div>
            `,

        methods: {
            init() {
                let that = this;
                ajax.get('api/bss.order/order-domain/initPayComponent', null, function(data) {
                    that.payments = data.payments;
                    that.payItemOptions = data.payItemOption;
                })
            },

            async valid() {
                let isFormValid = false;
                this.$refs['payForm'].validate(valid => {
                    isFormValid = valid;
                });

                if (!isFormValid) {
                    this.$message.error('填写信息不完整，请亲仔细检查哦~~~~~~~！');
                    return false;
                }

                if (this.payForm.needPay == 0) {
                    this.$message.error('收款金额不能为0~~~~~~~！');
                    return false;
                }

                const payMoneyError = await this.$refs.payMoney.fullValidate().catch(errMap => errMap);
                const payItemError = await this.$refs.payItem.fullValidate().catch(errMap => errMap);
                if (payMoneyError || payItemError) {
                    return false;
                }

                let totalPay = 0.00;
                if (this.payments) {
                    for (let payment of this.payments) {
                        totalPay += parseFloat(payment.money);
                    }
                }
                totalPay = totalPay.toFixed(2);

                let totalFee = 0.00;
                if (this.payItems) {
                    for (let item of this.payItems) {
                        totalFee += parseFloat(item.money);
                    }
                }
                totalFee = totalFee.toFixed(2);

                this.payForm.needPay = parseFloat(this.payForm.needPay).toFixed(2);
                if (this.payForm.needPay != totalFee) {
                    this.$message.error('收款金额与款项金额的合计不一致，请亲仔细检查哦~~~~~~~！');
                    return false;
                }

                if (totalPay != totalFee) {
                    this.$message.error('款项金额与付款金额不一致，请亲仔细检查哦~~~~~~~！');
                    return false;
                }
                return true;
            },
            footerMethod ({ columns, data }) {
                return [
                    columns.map((column, columnIndex) => {
                        if (['money'].includes(column.property)) {
                            let total = 0;
                            data.forEach(function(v, k) {
                                total += parseFloat(v.money);
                            })
                            return "合计: " + total.toFixed(2) + "元"
                        }
                        return '-'
                    })
                ]
            },

            popupDialog() {
                this.dialogVisible = true;
            },

            handlePayItem(value) {
                this.periodDisabled = false;
            },

            confirmSelectPayItem() {
                if (this.payItem.selectedPayItem == null || this.payItem.selectedPayItem.length === 0) {
                    return;
                }

                for (let items of this.payItem.selectedPayItem) {
                    let payItemName = this.findPayItemName(items, this.payItemOptions, '', 0);

                    let payItemValue = '';
                    let periodName = '';
                    let periodValue = '';
                    if (items[items.length-1].indexOf('period') < 0) {
                        payItemValue = items[items.length - 1];
                        periodName = '-';
                    } else {
                        let payItemNames = payItemName.split("-");
                        payItemName = '';
                        for (let i=0;i<payItemNames.length - 1;i++) {
                            payItemName += payItemNames[i] + "-";
                        }
                        payItemName = payItemName.substring(0, payItemName.length - 1);
                        periodName = payItemNames[payItemNames.length - 1];
                        payItemValue = items[items.length - 2];
                        periodValue = items[items.length - 1];
                    }
                    let payItem = {
                        payItemName : payItemName,
                        periodName : periodName,
                        payItemId: payItemValue,
                        period: periodValue,
                        money:0
                    };

                    let isFind = false;
                    for (let payItem of this.payItems) {
                        if (payItem.payItem == payItemValue) {
                            isFind = true;
                            break;
                        }
                    }
                    if (!isFind) {
                        this.payItems.push(payItem);
                    }
                }
                this.dialogVisible = false;
            },

            findPayItemName(payItems, payItemOptions, payItemName, initValue) {
                for (let item of payItemOptions) {
                    if (item.value == payItems[initValue]) {
                        initValue++;

                        payItemName += item.label+"-";
                        if (initValue == payItems.length) {
                            return payItemName.substring(0, payItemName.length-1);
                        } else {
                            return this.findPayItemName(payItems, item.children, payItemName, initValue)
                        }
                    }
                }
            },

            deletePayItem(row) {
                this.$confirm('此操作将删除款项, 是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    this.$refs.payItem.remove(row);
                    this.$refs.payItem.updateFooter();

                    for (let i=0;i<this.payItems.length;i++) {
                        let item = this.payItems[i];
                        if (item.payItemId == row.payItemId) {
                            this.payItems.splice(i,1);
                            break;
                        }
                    }
                });
            },

            getSubmitData() {
                let payDate = this.payForm.payDate;
                let needPay = this.payForm.needPay;
                let data = {
                    payDate : payDate,
                    needPay : needPay
                };
                for (let i=0;i<this.payItems.length;i++) {
                    data['payItems['+i+'].payItemId'] = this.payItems[i].payItemId.split("_")[1];
                    data['payItems['+i+'].money'] = this.payItems[i].money;
                }

                for (let i=0;i<this.payments.length;i++) {
                    data['payments['+i+'].paymentType'] = this.payments[i].paymentType;
                    data['payments['+i+'].money'] = this.payments[i].money;
                }
                return data;
            }
        },

        watch: {

        },

        mounted () {
            this.init();
        }
    });


})