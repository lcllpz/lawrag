// src/global.ts

const setRem = () => {
  const designWidth = 375; // 你的设计稿宽度，必须和 rootValue 对应
  const maxWidth = 600; // 你要求的 600px 封顶宽度

  // 1. 获取当前视口宽度
  let clientWidth = document.documentElement.clientWidth;

  // 2. 如果超过 600px，则按 600px 计算
  if (clientWidth > maxWidth) {
    clientWidth = maxWidth;
  }

  // 3. 计算 rem 基准值
  // 公式：当前宽度 / 设计稿宽度 * 预设的 rootValue
  // 这样当 clientWidth 是 375 时，fontSize 正好是 37.5px
  const rem = (clientWidth / designWidth) * 37.5;
  console.log(rem);

  // 4. 设置到 html 标签上
  document.documentElement.style.fontSize = `${rem}px`;
};

// 初始化执行
setRem();

// 窗口大小变化时重新执行
window.addEventListener("resize", setRem);
