<script lang="ts" setup>
import {
  Delete,
  Edit, WarningFilled
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import router from "@/router";

const isADMIN = ref(false)
const menuMap = reactive({})

onMounted(() => {
  axios({
    url: '/system/user',
    method: 'get'
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      isADMIN.value = (res.data.body.roleName == 'ADMIN')
      if (res.data.body.menus && res.data.body.menus.length > 0) {
        res.data.body.menus.forEach(item => {
          menuMap[item.url] = 'Y'
        })
      }
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
})

const formLabelWidth = ref('140px')
const data = reactive({
  actions: []
});

interface PropertyInfo {
  path: string
  name: string
  value: string
  strValue: string
}

const searchDialogFormVisible = ref(false)
const searchData = ref<PropertyInfo[]>()
const searchForm = reactive({
  path: '',
  name: '',
  value: '',
})
const searchProperty = () => {
  console.log('点击树节点查询配置列表:', searchForm)
  axios({
    url: '/queryAllProperty/v1.0',
    method: 'post',
    data: searchForm
  }).then((res: any) => {
    if (res.data.code == 1) {
      searchData.value = res.data.body
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const propForm = reactive({
  zkPath: '',
  name: '',
  value: ''
})
const dialogFormVisible = ref(false)
const updateProperty = (scope: any) => {
  console.log('更新配置: %o', scope.row)
  propForm.zkPath = scope.row.path
  propForm.name = scope.row.name
  propForm.value = scope.row.strValue
  dialogFormVisible.value = true
}
const deleteProperty = (scope: any) => {
  console.log('删除配置: %o', scope)
  axios({
    url: '/deleteLeaves/v1.0',
    method: 'post',
    data: {
      zkPath: scope.row.path,
      leafNames: [scope.row.name]
    }
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('删除成功', 'success')
      searchProperty()
    } else {
      msg(res.data.msg, 'error')
    }
  })
}

const updateConfig = () => {
  axios({
    url: '/updateProperty/v1.0',
    method: 'post',
    data: propForm
  }).then((res: any) => {
    if (res.data.code == 1) {
      msg('更新成功', 'success')
      dialogFormVisible.value = false
    } else {
      msg(res.data.msg, 'error')
    }
  })
}

const queryActions = () => {
}

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

onMounted(() => {
  console.log('页面加载后')
  queryActions()
});

const debounce = (callback: (...args: any[]) => void, delay: number) => {
  let tid: any;
  return function (...args: any[]) {
    const ctx = self;
    tid && clearTimeout(tid);
    tid = setTimeout(() => {
      callback.apply(ctx, args);
    }, delay);
  };
};

const _ = (window as any).ResizeObserver;
(window as any).ResizeObserver = class ResizeObserver extends _ {
  constructor(callback: (...args: any[]) => void) {
    callback = debounce(callback, 20);
    super(callback);
  }
};

</script>

<template>
  <div class="container" v-if="isADMIN || menuMap['search']">
    <el-link type="success" @click="router.push({path:'/main'})">返回主页</el-link>
    <el-divider content-position="left">查询条件</el-divider>
    <el-form :model="searchForm" :inline="true" size="small">
      <el-form-item label="目录：">
        <el-input v-model="searchForm.path" type="text"/>
      </el-form-item>
      <el-form-item label="属性名：">
        <el-input v-model="searchForm.name" type="text"/>
      </el-form-item>
      <el-form-item label="属性值：">
        <el-input v-model="searchForm.value" type="text"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="searchProperty">查询</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">查询结果</el-divider>
    <el-table :data="searchData" style="width: 100%" :border="true" table-layout="fixed" :stripe="true" size="small"
              :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="120" header-align="center" align="center">
        <template #default="scope">
          <el-button :icon="Edit" circle size="small" title="编辑" @click="updateProperty(scope)"/>
          <el-popconfirm title="你确定要删除本条配置吗?" @confirm="deleteProperty(scope)" :icon="WarningFilled"
                         confirm-button-type="danger" cancel-button-type="info" icon-color="red">
            <template #reference>
              <el-button :icon="Delete" circle size="small" title="删除" v-if="isADMIN || menuMap['delete']"/>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="目录" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="name" label="属性名" :show-overflow-tooltip="true" header-align="center"/>
      <el-table-column prop="strValue" label="属性值" :show-overflow-tooltip="true" header-align="center"/>
    </el-table>

    <el-dialog v-model="dialogFormVisible" title="编辑属性">
      <el-form :model="propForm" size="small">
        <el-form-item label="属性名：">
          <el-input v-model="propForm.name" :type="propForm.name.length>50?'textarea':'text'" rows="3" disabled/>
        </el-form-item>
        <el-form-item label="属性值：">
          <el-input v-model="propForm.value" type="textarea" :rows="7"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogFormVisible = false">取消</el-button>
          <el-button type="primary" @click="updateConfig">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.container {
  flex-grow: 1;
  padding: 20px 3%;
  overflow: auto;
  width: 80%;

}
</style>s