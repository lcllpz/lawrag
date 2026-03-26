// postcss.config.js
module.exports = {
  plugins: {
    "postcss-pxtorem": {
      rootValue: 37.5, // 确保这里是 37.5
      propList: ["*"],
      unitPrecision: 5,
      selectorBlackList: ["#root"], // 忽略 #root 选择器下的所有 px 转换
      replace: true,
      mediaQuery: false,
      minPixelValue: 2,
      // 或者精准包含 antd-mobile
      exclude: (file) => {
        // 如果你希望 antd-mobile 也转，就只排除掉其他的 node_modules
        if (file.indexOf("antd-mobile") !== -1) return false;
        return /node_modules/i.test(file);
      },
    },
  },
};
