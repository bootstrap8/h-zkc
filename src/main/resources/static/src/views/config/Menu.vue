<script lang="ts" setup>
import {
  Edit
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import router from "@/router";

const isADMIN = ref(false)

onMounted(() => {
  axios({
    url: '/system/user',
    method: 'get'
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      isADMIN.value = (res.data.body.roleName == 'ADMIN')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
})

const formLabelWidth = ref('140px')
const form = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  url: ''
})
const data = reactive({
  menus: [],
  total: 0
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

const queryMenuList = () => {
  axios({
    url: '/menus/list',
    method: 'post',
    data: form,
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.menus = res.data.body.list
      data.total = res.data.body.total
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref('新增菜单')
const menuForm = reactive({
  id: 0,
  name: '',
  url: ''
})
const formRef = ref<FormInstance>()
const rules = reactive<FormRules>({
  name: [
    {required: true, message: '不能为空', trigger: 'blur'},
  ],
  url: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ]
})

const updateMenu = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/menus/menu',
        method: dialogTitle.value == '新增菜单' ? 'post' : 'put',
        data: menuForm
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, 'success')
          dialogFormVisible.value = false
          queryMenuList()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}

const showMenuAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = '新增菜单'
}

const showMenuEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = '编辑菜单'
  menuForm.id = scope.row.id
  menuForm.name = scope.row.name
  menuForm.url = scope.row.url
}

const deleteMenu = (scope) => {
  axios({
    url: '/menus/menu',
    method: 'delete',
    params: {id: scope.row.id}
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryMenuList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

onMounted(() => {
  console.log('页面加载后')
  queryMenuList()
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
  <div class="container" v-if="isADMIN">
    <el-link type="success" @click="router.push({path:'/main'})">返回主页</el-link>
    <el-divider content-position="left">查询条件</el-divider>
    <el-form :model="form" size="small" label-position="right" inline-message :inline="true">
      <el-form-item label="菜单名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入..."/>
      </el-form-item>
      <el-form-item label="菜单url" prop="url">
        <el-input v-model="form.url" placeholder="请输入..."/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryMenuList()">查询</el-button>
        <el-button type="success" :icon="Edit" circle @click="showMenuAddDialog()" title="新增菜单"/>
      </el-form-item>

      <el-divider content-position="left">查询结果</el-divider>
      <el-table :data="data.menus" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
                size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
        <el-table-column fixed="left" label="操作" width="180" header-align="center" align="center">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="showMenuEditDialog(scope)">编辑
            </el-button>
            <el-popconfirm title="你确定要删除本条记录吗?" @confirm="deleteMenu(scope)"
                           icon-color="red"
                           confirm-button-type="danger">
              <template #reference>
                <el-button link type="danger" size="small">删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="ID" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="name" label="菜单名称" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="url" label="菜单url" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="parentId" label="父ID" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="orderIndex" label="顺序" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="fmtCreatedAt" label="创建时间" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
        <el-table-column prop="fmtUpdatedAt" label="修改时间" :show-overflow-tooltip="true" header-align="center"
                         align="center"/>
      </el-table>
      <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                     layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                     @size-change="queryMenuList()"
                     @current-change="queryMenuList()" @prev-click="queryMenuList()" @next-click="queryMenuList()"
                     :small="true" :background="true"
                     :page-sizes="[5, 10, 20, 50, 100]"/>
    </el-form>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle">
      <el-form :model="menuForm" label-position="right" size="small" :inline="false" ref="formRef" :rules="rules"
               label-width="20%">
        <el-form-item label="菜单名称：" prop="name">
          <el-input v-model="menuForm.name" type="text"/>
        </el-form-item>
        <el-form-item label="菜单url：" prop="url">
          <el-input v-model="menuForm.url" type="text"/>
        </el-form-item>
      </el-form>
      <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogFormVisible = false">取消</el-button>
              <el-button type="primary" @click="updateMenu(formRef)">保存</el-button>
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
</style>