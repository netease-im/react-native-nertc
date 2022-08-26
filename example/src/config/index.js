export default {
  // 初始化引擎配置
  setupOptions: {
    appKey: '', // your appkey
    logDir: '', // expected log directory
    logLevel: 3,
  },
  // 已获取的 NERTC Token
  // 安全模式下必须设置为获取到的 Token 。若未传入正确的 Token 将无法进入房间。推荐使用安全模式
  // 调试模式下可设置为 空。安全性不高，建议在产品正式上线前在云信控制台中将鉴权方式恢复为默认的安全模式
  // token 获取参考： [ token 获取 ](https://doc.yunxin.163.com/docs/jcyOTA0ODM/TQ0MTI2ODQ?platformId=50002)
  token: '',
};
