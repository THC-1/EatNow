<script>
  export default {
    onLaunch() {
      console.log('EatNow MiniProgram Launch')
    }
  }
</script>

<style>
  page {
    min-height: 100%;
    background: #fff7ea;
    color: #2b2118;
    font-family: PingFang SC, Microsoft YaHei, sans-serif;
  }

  view,
  text,
  button,
  input,
  textarea {
    box-sizing: border-box;
  }

  button {
    margin: 0;
    padding: 0;
    line-height: 1;
    border-radius: 0;
    background: transparent;
  }

  button::after {
    border: 0;
  }

  .page {
    min-height: 100vh;
    background:
      radial-gradient(circle at 12% 0%, rgba(255, 214, 78, 0.36), transparent 340rpx),
      linear-gradient(180deg, #fff4df 0%, #fffaf2 52%, #f7fbec 100%);
    padding: 28rpx;
  }

  .section {
    margin-top: 34rpx;
  }

  .section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 18rpx;
  }

  .section-title {
    color: #241811;
    font-size: 34rpx;
    font-weight: 800;
    letter-spacing: 0;
  }

  .section-link {
    color: #e94b35;
    font-size: 24rpx;
    font-weight: 700;
  }

  .pill {
    display: inline-flex;
    align-items: center;
    min-height: 44rpx;
    padding: 0 18rpx;
    border-radius: 999rpx;
    background: #fff;
    color: #7b6046;
    font-size: 23rpx;
    font-weight: 700;
    border: 2rpx solid rgba(43, 33, 24, 0.08);
  }

  .primary-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 88rpx;
    border-radius: 999rpx;
    background: #e94b35;
    color: #fffdf7;
    font-size: 29rpx;
    font-weight: 800;
    box-shadow: 0 18rpx 34rpx rgba(233, 75, 53, 0.22);
  }

  .ghost-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 76rpx;
    border-radius: 999rpx;
    background: #fffdf7;
    color: #2b2118;
    font-size: 26rpx;
    font-weight: 800;
    border: 2rpx solid rgba(43, 33, 24, 0.1);
  }

  .food-art {
    position: relative;
    overflow: hidden;
    border-radius: 20rpx;
    background:
      radial-gradient(circle at 72% 26%, rgba(255, 255, 255, 0.76) 0 24rpx, transparent 25rpx),
      radial-gradient(circle at 27% 74%, rgba(127, 189, 65, 0.5) 0 34rpx, transparent 35rpx),
      linear-gradient(135deg, #ffd64e 0%, #ff9f43 54%, #e94b35 100%);
  }

  .food-art::after {
    content: "";
    position: absolute;
    right: -20rpx;
    bottom: -28rpx;
    width: 116rpx;
    height: 116rpx;
    border-radius: 50%;
    background: rgba(255, 253, 247, 0.42);
  }

  .empty {
    padding: 54rpx 20rpx;
    color: #9d8466;
    text-align: center;
    font-size: 26rpx;
  }
</style>
