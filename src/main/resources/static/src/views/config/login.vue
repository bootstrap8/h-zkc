<script lang="ts" setup>
import {
  Edit
} from '@element-plus/icons-vue'
import {ref, reactive, onMounted, computed} from 'vue'
import axios from '@/network'
import {msg} from '@/utils/Utils'
import type {FormInstance, FormRules} from 'element-plus'
import JSEncrypt from 'jsencrypt'
import CryptoJS from "crypto-js"
import router from "@/router";

function obfuscate(input, key) {
  let result = '';
  for (let i = 0; i < input.length; i++) {
    result += String.fromCharCode(input.charCodeAt(i) ^ key);
  }
  return result;
}

// XOR 还原算法（解密）
function deobfuscate(input, key) {
  return obfuscate(input, key);
}

const obfs = "969";
const key = CryptoJS.enc.Utf8.parse(deobfuscate("΍ΊϻΌΌϱϰϺϸΌϽϺϽΈϽϽ", obfs));
const iv = key

//解密方法
const decryptAES = (word) => {
  let base64 = CryptoJS.enc.Base64.parse(word);
  let src = CryptoJS.enc.Base64.stringify(base64);
  // 解密模式为CBC，补码方式为PKCS5Padding（也就是PKCS7）
  let decrypt = CryptoJS.AES.decrypt(src, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  });
  let decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
  return decryptedStr.toString();
}

//加密方法
const encryptAES = (word) => {
  let srcs = CryptoJS.enc.Utf8.parse(word);
  // 加密模式为CBC，补码方式为PKCS5Padding（也就是PKCS7）
  let encrypted = CryptoJS.AES.encrypt(srcs, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7
  });
  //返回base64
  return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);
}

const data = reactive({
  actions: []
});

const headerCellStyle = () => {
  // 添加表头颜色
  return {backgroundColor: '#f5f5f5', color: '#333', fontWeight: 'bold'};
}

onMounted(() => {
  console.log('页面加载后')
});

const username = ref('');
const password = ref('');
const showModal = ref(false);
const question = ref('');
const correctAnswer = ref<number | null>(null);
const userAnswer = ref<number | null>(null);

function generateQuestion() {
  const num1 = Math.floor(Math.random() * 10);
  const num2 = Math.floor(Math.random() * 10);
  correctAnswer.value = num1 + num2;
  question.value = `请计算: ${num1} + ${num2} = ?`;
}

function validateLogin() {
  showModal.value = true;
  generateQuestion();
  return false;
}

function confirmAnswer() {
  if (userAnswer.value === correctAnswer.value) {
    // alert("验证成功，正在登录...");
    showModal.value = false;
    const form = {
      username: username.value,
      password: password.value
    }
    const body = encryptAES(JSON.stringify(form))
    axios({
      url: '/system/login',
      method: 'post',
      headers: {
        'Content-Type': 'application/json;charset=utf8'
      },
      data: body
    }).then((res: any) => {
      if (res.data.state == 'OK') {
        router.push({path: '/main'})
      } else {
        msg(res.data.errorMessage, 'warning')
      }
    }).catch((err: Error) => {
      msg('请求异常', 'error')
    })
  } else {
    alert("答案错误，请重试！");
  }
}

function cancel() {
  showModal.value = false;
}

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
  <div class="container">
    <div class="left-panel"></div>
    <div class="right-panel">
      <div class="login-form">
        <h2>登录</h2>
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <button type="button" @click="validateLogin">登录</button>
      </div>
    </div>

    <!-- 自定义模态框 -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <h3>安全保护</h3>
        <p>检测到本次操作需输入验证码</p>
        <span @click="generateQuestion">{{ question }}</span>
        <input type="number" v-model="userAnswer" placeholder="请输入验证码">
        <div class="modal-buttons">
          <button @click="confirmAnswer">确定</button>
          <button @click="cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  height: 100vh;
}

.left-panel {
  flex: 1;
  background: url('@/assets/login.png') no-repeat center center;
  background-size: 80%;
}

.right-panel {
  width: 40%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: white;
}

.login-form {
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  width: 80%;
  max-width: 400px;
}

.login-form h2 {
  margin-bottom: 20px;
  text-align: center;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
}

.form-group input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #007BFF;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

.modal {
  display: flex;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0, 0, 0, 0.4);
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: #fefefe;
  padding: 20px;
  border: 1px solid #888;
  width: 300px;
  text-align: center;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

.modal-content h3 {
  margin-top: 0;
  color: #ff5722;
}

.modal-content p {
  margin: 10px 0;
}

.modal-content input {
  width: 80%;
  padding: 8px;
  margin-top: 10px;
  margin-bottom: 10px;
}

.modal-buttons {
  display: flex;
  justify-content: space-around;
}

.modal-buttons button {
  padding: 10px 20px;
  background-color: #007BFF;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  width: 45%;
}

.modal-buttons button:hover {
  background-color: #0056b3;
}
</style>