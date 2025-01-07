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
  username: '',
  roleId: ''
})
const data = reactive({
  users: [],
  total: 0,
  roles: []
});

const queryUserList = () => {
  axios({
    url: '/users/list',
    method: 'post',
    data: form,
    params: {
      pageNum: form.pageNum,
      pageSize: form.pageSize
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.users = res.data.body.list
      data.total = res.data.body.total
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible = ref(false)
const dialogTitle = ref('新增用户')
const userForm = reactive({
  id: 0,
  username: '',
  password: '',
  password2: '',
  roleId: ''
})
const formRef = ref<FormInstance>()
const rules = reactive<FormRules>({
  username: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ],
  password2: [
    {required: true, message: '不能为空', trigger: 'blur'},
    {
      required: true, trigger: 'blur', validator: (rule: any, value: any, callback: any) => {
        if (userForm.password != userForm.password2) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }
    }
  ],
  roleId: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ],
})

const showUserAddDialog = () => {
  dialogFormVisible.value = true
  dialogTitle.value = '新增用户'
  queryRoleList()
}

const showUserEditDialog = (scope) => {
  dialogFormVisible.value = true
  dialogTitle.value = '编辑用户'
  userForm.id = scope.row.id
  userForm.username = scope.row.username
  userForm.roleId = scope.row.roleId
  queryRoleList()
}

const queryRoleList = () => {
  axios({
    url: '/roles/list',
    method: 'post',
    data: {},
    params: {
      pageNum: -1
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      data.roles = res.data.body.list
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const updateUser = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/users/user',
        method: dialogTitle.value == '新增用户' ? 'post' : 'put',
        data: userForm
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, 'success')
          dialogFormVisible.value = false
          queryUserList()
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}

const deleteUser = (scope) => {
  axios({
    url: '/users/user',
    method: 'delete',
    params: {
      id: scope.row.id
    }
  }).then((res: any) => {
    if (res.data.state == 'OK') {
      msg(res.data.body, 'success')
      queryUserList()
    } else {
      msg(res.data.errorMessage, 'warning')
    }
  }).catch((err: Error) => {
    msg('请求异常', 'error')
  })
}

const dialogFormVisible2 = ref(false)
const dialogTitle2 = ref('修改密码')
const passForm = reactive({
  id: '',
  oldPassword: '',
  newPassword: '',
  newPassword2: ''
})
const formRef2 = ref<FormInstance>()
const rules2 = reactive<FormRules>({
  oldPassword: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '不能为空', trigger: 'blur'}
  ],
  newPassword2: [
    {required: true, message: '不能为空', trigger: 'blur'},
    {
      required: true, trigger: 'blur', validator: (rule: any, value: any, callback: any) => {
        if (passForm.newPassword != passForm.newPassword2) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      }
    }
  ]
})

const modifyPassword = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      axios({
        url: '/users/user/pass',
        method: 'put',
        data: passForm
      }).then((res: any) => {
        if (res.data.state == 'OK') {
          msg(res.data.body, 'success')
          dialogFormVisible2.value = false
        } else {
          msg(res.data.errorMessage, 'warning')
        }
      }).catch((err: Error) => {
        msg('请求异常', 'error')
      })
    }
  })
}

const showPasswordModifyDialog = (scope) => {
  passForm.id=scope.row.id
  dialogFormVisible2.value = true
}

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

onMounted(() => {
  console.log('页面加载后')
  queryUserList()
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
    <el-form :model="form" size="small" label-position="right" inline-message inline>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入..." type="text"/>
      </el-form-item>
      <el-form-item label="角色" prop="roleId">
        <el-select v-model="form.roleId" placeholder="请选择" size="small">
          <el-option :key="role.id" :label="role.name" :value="role.id" v-for="role in data.roles"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" size="small" @click="queryUserList()">查询</el-button>
        <el-button type="success" :icon="Edit" circle @click="showUserAddDialog()" title="新增用户"/>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">查询结果</el-divider>
    <el-table :data="data.users" style="width: 100%" :border="true" table-layout="fixed" :stripe="true"
              size="small" :highlight-current-row="true" :header-cell-style="headerCellStyle">
      <el-table-column fixed="left" label="操作" width="180" header-align="center" align="center">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="showUserEditDialog(scope)">编辑</el-button>
          <el-popconfirm title="你确定要删除本条记录吗?" @confirm="deleteUser(scope)"
                         icon-color="red"
                         confirm-button-type="danger">
            <template #reference>
              <el-button link type="danger" size="small">删除
              </el-button>
            </template>
          </el-popconfirm>
          <el-button link type="primary" size="small" @click="showPasswordModifyDialog(scope)">修改密码</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="用户ID" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="username" label="用户名" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="roleName" label="角色" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtCreatedAt" label="新建时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
      <el-table-column prop="fmtUpdatedAt" label="修改时间" :show-overflow-tooltip="true" header-align="center"
                       align="center"/>
    </el-table>
    <el-pagination class="page" v-model:page-size="form.pageSize" v-model:current-page="form.pageNum"
                   layout="->, total, sizes, prev, pager, next, jumper" v-model:total="data.total"
                   @size-change="queryUserList()"
                   @current-change="queryUserList()" @prev-click="queryUserList()" @next-click="queryUserList()"
                   :small="true" :background="true"
                   :page-sizes="[5, 10, 20, 50, 100]"/>

    <el-dialog v-model="dialogFormVisible" :title="dialogTitle">
      <el-form :model="userForm" label-position="right" size="small" :inline="false" ref="formRef" :rules="rules"
               label-width="20%">
        <el-form-item label="用户名：" prop="username">
          <el-input v-model="userForm.username" type="text" :disabled="dialogTitle=='编辑用户'"/>
        </el-form-item>
        <el-form-item label="密码：" prop="password" v-if="dialogTitle=='新增用户'">
          <el-input v-model="userForm.password" type="password"/>
        </el-form-item>
        <el-form-item label="二次确认密码：" prop="password2" v-if="dialogTitle=='新增用户'">
          <el-input v-model="userForm.password2" type="password"/>
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="userForm.roleId" placeholder="请选择" size="small">
            <el-option :key="role.id" :label="role.name" :value="role.id" v-for="role in data.roles"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible = false">取消</el-button>
                  <el-button type="primary" @click="updateUser(formRef)">保存</el-button>
                </span>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogFormVisible2" :title="dialogTitle2">
      <el-form :model="passForm" label-position="right" size="small" :inline="false" ref="formRef2" :rules="rules2"
               label-width="20%">
        <el-form-item label="老密码：" prop="oldPassword">
          <el-input v-model="passForm.oldPassword" type="password"/>
        </el-form-item>
        <el-form-item label="新密码：" prop="newPassword">
          <el-input v-model="passForm.newPassword" type="password"/>
        </el-form-item>
        <el-form-item label="二次新密码确认：" prop="newPassword2">
          <el-input v-model="passForm.newPassword2" type="password"/>
        </el-form-item>
      </el-form>
      <template #footer>
                <span class="dialog-footer">
                  <el-button @click="dialogFormVisible2 = false">取消</el-button>
                  <el-button type="primary" @click="modifyPassword(formRef2)">修改</el-button>
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