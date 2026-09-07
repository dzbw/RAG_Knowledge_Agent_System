<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus, Refresh, Setting } from '@element-plus/icons-vue'
import {
  fetchInvoicePage,
  statInvoicesByDepartment,
  saveInvoice,
  deleteInvoice,
  listDepartments,
  saveDepartment,
  deleteDepartment,
} from '../../api/finance'
import { formatDateTime, formatDate } from '../../utils/date'

const INVOICE_TYPES = ['增值税专用发票', '增值税普通发票', '电子发票', '其他']

// ---------- 列表 ----------
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const fDept = ref(null)
const fStatus = ref(null)

async function load() {
  loading.value = true
  try {
    const res = await fetchInvoicePage({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      departmentId: fDept.value || undefined,
      status: fStatus.value ?? undefined,
    })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function resetFilter() {
  keyword.value = ''
  fDept.value = null
  fStatus.value = null
  search()
}

// ---------- 部门与统计 ----------
const depts = ref([])
const statList = ref([])

async function loadDepts() {
  const res = await listDepartments()
  depts.value = res.data
}

async function loadStat() {
  const res = await statInvoicesByDepartment()
  statList.value = res.data
}

async function loadAll() {
  load()
  loadDepts()
  loadStat()
}

const statSummary = computed(() => {
  const sum = (f) =>
    statList.value.reduce((acc, r) => acc + (Number(r[f]) || 0), 0)
  return {
    count: sum('count'),
    amount: sum('totalAmount').toFixed(2),
    tax: sum('totalTax').toFixed(2),
    total: (sum('totalAmount') + sum('totalTax')).toFixed(2),
  }
})

// ---------- 发票新增/编辑 ----------
const dlg = ref(false)
const form = ref(emptyForm())

function emptyForm() {
  return {
    id: null,
    invoiceNo: '',
    invoiceType: '增值税专用发票',
    supplier: '',
    departmentId: null,
    amount: 0,
    taxAmount: 0,
    invoiceDate: '',
    status: 1,
    remark: '',
  }
}

function openCreate() {
  form.value = emptyForm()
  dlg.value = true
}

function openEdit(row) {
  form.value = {
    id: row.id,
    invoiceNo: row.invoiceNo,
    invoiceType: row.invoiceType,
    supplier: row.supplier,
    departmentId: row.departmentId,
    amount: Number(row.amount),
    taxAmount: Number(row.taxAmount),
    invoiceDate: row.invoiceDate ? String(row.invoiceDate).slice(0, 10) : '',
    status: row.status,
    remark: row.remark || '',
  }
  dlg.value = true
}

const formTotal = computed(() =>
  ((Number(form.value.amount) || 0) + (Number(form.value.taxAmount) || 0)).toFixed(2),
)

async function save() {
  if (!form.value.invoiceNo.trim()) return ElMessage.warning('请填写发票号码')
  if (!form.value.supplier.trim()) return ElMessage.warning('请填写销售方')
  if (!form.value.departmentId) return ElMessage.warning('请选择所属部门')
  if (!form.value.invoiceDate) return ElMessage.warning('请选择开票日期')
  await saveInvoice(form.value)
  ElMessage.success('保存成功')
  dlg.value = false
  loadAll()
}

async function del(row) {
  await ElMessageBox.confirm(`确定删除发票「${row.invoiceNo}」？`, '提示')
  await deleteInvoice(row.id)
  ElMessage.success('删除成功')
  loadAll()
}

// ---------- 部门管理 ----------
const deptDlg = ref(false)
const deptForm = ref({ id: null, name: '', remark: '' })
const deptLoading = ref(false)

async function loadDeptList() {
  deptLoading.value = true
  try {
    await loadDepts()
  } finally {
    deptLoading.value = false
  }
}

function openDeptCreate() {
  deptForm.value = { id: null, name: '', remark: '' }
  deptDlg.value = true
}

function openDeptEdit(row) {
  deptForm.value = { id: row.id, name: row.name, remark: row.remark || '' }
  deptDlg.value = true
}

async function saveDept() {
  if (!deptForm.value.name.trim()) return ElMessage.warning('请填写部门名称')
  await saveDepartment(deptForm.value)
  deptDlg.value = false
  ElMessage.success('保存成功')
  loadDeptList()
  loadStat()
}

async function delDept(row) {
  await ElMessageBox.confirm(`确定删除部门「${row.name}」？`, '提示')
  await deleteDepartment(row.id)
  ElMessage.success('删除成功')
  loadDeptList()
  loadStat()
}

function money(v) {
  return (Number(v) || 0).toFixed(2)
}

onMounted(loadAll)
</script>

<template>
  <div>
    <div class="page-title">进项发票入账</div>

    <!-- 汇总统计卡片 -->
    <el-row :gutter="14" class="cards">
      <el-col :span="6">
        <div class="card c1">
          <div class="k">发票总张数</div>
          <div class="v">{{ statSummary.count }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card c2">
          <div class="k">不含税金额合计</div>
          <div class="v">¥ {{ statSummary.amount }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card c3">
          <div class="k">税额合计</div>
          <div class="v">¥ {{ statSummary.tax }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="card c4">
          <div class="k">价税合计</div>
          <div class="v">¥ {{ statSummary.total }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- 部门统计 -->
    <el-card class="box" shadow="hover">
      <template #header>
        <div class="hd">
          <span>各部门入账统计</span>
          <el-button size="small" :icon="Refresh" @click="loadStat">刷新</el-button>
        </div>
      </template>
      <el-table :data="statList" stripe size="small">
        <el-table-column prop="deptName" label="部门" min-width="140" />
        <el-table-column prop="count" label="发票张数" width="110" align="right" />
        <el-table-column label="不含税金额" width="150" align="right">
          <template #default="{ row }">¥ {{ money(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="税额" width="140" align="right">
          <template #default="{ row }">¥ {{ money(row.totalTax) }}</template>
        </el-table-column>
        <el-table-column label="价税合计" width="150" align="right">
          <template #default="{ row }">¥ {{ money(row.totalAmount + row.totalTax) }}</template>
        </el-table-column>
        <el-table-column label="占比" min-width="180">
          <template #default="{ row }">
            <el-progress
              :percentage="statSummary.amount ? Number(((row.totalAmount / Number(statSummary.amount)) * 100).toFixed(1)) : 0"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发票明细 -->
    <el-card class="box" shadow="hover">
      <template #header>
        <div class="hd">
          <span>入账明细</span>
          <el-button size="small" :icon="Setting" @click="openDeptCreate">管理部门</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索发票号/供应商" clearable style="width: 200px" @keyup.enter="search" @clear="search" />
        <el-select v-model="fDept" placeholder="全部部门" clearable style="width: 150px" @change="search">
          <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
        </el-select>
        <el-select v-model="fStatus" placeholder="全部状态" clearable style="width: 130px" @change="search">
          <el-option label="已入账" :value="1" />
          <el-option label="未入账" :value="0" />
        </el-select>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
        <el-button type="success" :icon="Plus" @click="openCreate">新增发票</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="invoiceNo" label="发票号码" min-width="150" />
        <el-table-column prop="invoiceType" label="发票类型" width="150" />
        <el-table-column prop="supplier" label="销售方" min-width="160" show-overflow-tooltip />
        <el-table-column prop="departmentName" label="所属部门" width="110" />
        <el-table-column label="不含税金额" width="130" align="right">
          <template #default="{ row }">¥ {{ money(row.amount) }}</template>
        </el-table-column>
        <el-table-column label="税额" width="110" align="right">
          <template #default="{ row }">¥ {{ money(row.taxAmount) }}</template>
        </el-table-column>
        <el-table-column label="价税合计" width="130" align="right">
          <template #default="{ row }">
            <b>¥ {{ money(row.amount + row.taxAmount) }}</b>
          </template>
        </el-table-column>
        <el-table-column label="开票日期" width="110">
          <template #default="{ row }">{{ formatDate(row.invoiceDate) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已入账' : '未入账' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="录入时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="del(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pg"
        background
        layout="total, prev, pager, next"
        :total="total"
        v-model:page-size="size"
        v-model:current-page="page"
        @current-change="load"
      />
    </el-card>

    <!-- 发票新增/编辑弹窗 -->
    <el-dialog v-model="dlg" title="发票信息" width="520px" destroy-on-close>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="发票号码">
              <el-input v-model="form.invoiceNo" placeholder="发票号码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发票类型">
              <el-select v-model="form.invoiceType" style="width: 100%">
                <el-option v-for="t in INVOICE_TYPES" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="销售方">
          <el-input v-model="form.supplier" placeholder="供应商 / 销售方名称" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属部门">
              <el-select v-model="form.departmentId" placeholder="选择部门" style="width: 100%">
                <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开票日期">
              <el-date-picker
                v-model="form.invoiceDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="不含税金额">
              <el-input-number v-model="form.amount" :min="0" :precision="2" :step="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="税额">
              <el-input-number v-model="form.taxAmount" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="价税合计">
          <el-tag type="primary" size="large">¥ {{ formTotal }}</el-tag>
        </el-form-item>
        <el-form-item label="入账状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已入账</el-radio>
            <el-radio :value="0">未入账</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 部门管理弹窗 -->
    <el-dialog v-model="deptDlg" title="部门管理" width="560px" destroy-on-close>
      <div class="toolbar">
        <el-button type="success" size="small" :icon="Plus" @click="openDeptCreate">新增部门</el-button>
        <span class="tip">删除部门前，需先移除该部门下的发票</span>
      </div>
      <el-table :data="depts" v-loading="deptLoading" stripe size="small" max-height="320">
        <el-table-column prop="name" label="部门名称" min-width="140" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDeptEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="delDept(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="dept-form">
        <el-form :model="deptForm" label-width="80px" size="small">
          <el-form-item :label="deptForm.id ? '部门名称' : '新部门名称'">
            <el-input v-model="deptForm.name" placeholder="如：技术部" style="width: 220px" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="deptForm.remark" placeholder="可选" style="width: 220px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="saveDept">{{ deptForm.id ? '保存修改' : '新增部门' }}</el-button>
            <el-button v-if="deptForm.id" size="small" @click="deptForm = { id: null, name: '', remark: '' }">重置为新增</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.cards {
  margin-bottom: 14px;
}
.card {
  border-radius: 14px;
  padding: 16px 18px;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
}
.card.c2 {
  background: linear-gradient(135deg, #10b981, #14b8a6);
}
.card.c3 {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}
.card.c4 {
  background: linear-gradient(135deg, #ec4899, #ef4444);
}
.card .k {
  font-size: 12px;
  opacity: 0.85;
}
.card .v {
  font-size: 24px;
  font-weight: 800;
  margin-top: 6px;
}
.box {
  border-radius: 16px;
  margin-bottom: 14px;
}
.hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
  align-items: center;
}
.tip {
  font-size: 12px;
  color: #94a3b8;
}
.pg {
  margin-top: 16px;
  justify-content: flex-end;
}
.dept-form {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}
</style>
