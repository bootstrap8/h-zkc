const {defineConfig} = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  outputDir: 'hbq969-zkc',
  publicPath: './',
  devServer: {
    proxy: {
      '/dev': {
        target: 'http://localhost:30145',
        changeOrigin: true,
        secure: false,
        pathRewrite: { '^/dev': '' },
      },
    },
  }
})
