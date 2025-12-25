<template>
  <div class="discover-container">
    <div class="discover-header">
      <h1>发现</h1>
      <p>智能匹配，让做菜更简单</p>
    </div>

    <div class="feature-cards">
      <!-- 根据菜品买菜 -->
      <div class="feature-card" @click="goToShopByDish">
        <div class="feature-icon shop">
          <el-icon><ShoppingCart /></el-icon>
        </div>
        <div class="feature-content">
          <h3>根据菜品买菜</h3>
          <p>输入你想要做的菜品，系统会为你整理出需要购买的食材清单，并按超市区域分类，让购物更高效</p>
          <div class="feature-tags">
            <el-tag size="small" type="success">智能分类</el-tag>
            <el-tag size="small" type="info">区域规划</el-tag>
            <el-tag size="small" type="warning">购物卡片</el-tag>
          </div>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 根据食材找菜品 -->
      <div class="feature-card" @click="goToFindDishes">
        <div class="feature-icon find">
          <el-icon><Search /></el-icon>
        </div>
        <div class="feature-content">
          <h3>根据食材找菜品</h3>
          <p>输入你已有的食材，系统会智能推荐可以制作的菜品，并提示哪些食材易过期，建议优先使用</p>
          <div class="feature-tags">
            <el-tag size="small" type="success">智能推荐</el-tag>
            <el-tag size="small" type="danger">过期提醒</el-tag>
            <el-tag size="small" type="warning">匹配度排序</el-tag>
          </div>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 营养信息查询 -->
      <div class="feature-card" @click="goToNutrition">
        <div class="feature-icon nutrition">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="feature-content">
          <h3>营养信息查询</h3>
          <p>输入菜品名称，查看卡路里、蛋白质、脂肪等详细营养信息，并自动计算多道菜品的营养总和</p>
          <div class="feature-tags">
            <el-tag size="small" type="success">营养分析</el-tag>
            <el-tag size="small" type="info">数据统计</el-tag>
            <el-tag size="small" type="warning">健康管理</el-tag>
          </div>
        </div>
        <div class="feature-arrow">
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ShoppingCart, Search, TrendCharts, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const goToShopByDish = () => {
  router.push('/discover/shop-by-dish')
}

const goToFindDishes = () => {
  router.push('/discover/find-dishes')
}

const goToNutrition = () => {
  router.push('/discover/nutrition')
}
</script>

<style scoped>
.discover-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

.discover-header {
  text-align: center;
  margin-bottom: 60px;
  padding: 40px 0;
}

.discover-header h1 {
  font-size: 48px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.discover-header p {
  font-size: 20px;
  color: #666;
  font-weight: 300;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 32px;
  margin-top: 40px;
}

.feature-card {
  position: relative;
  background: #ffffff;
  border-radius: 20px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
}

.feature-card:hover::before {
  transform: scaleX(1);
}

.feature-card.shop:hover {
  border-color: #67c23a;
}

.feature-card.shop::before {
  background: linear-gradient(90deg, #67c23a, #95d475);
}

.feature-card.find:hover {
  border-color: #409eff;
}

.feature-card.find::before {
  background: linear-gradient(90deg, #409eff, #66b1ff);
}

.feature-card.nutrition:hover {
  border-color: #e6a23c;
}

.feature-card.nutrition::before {
  background: linear-gradient(90deg, #e6a23c, #f0a020);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #fff;
  margin-bottom: 8px;
}

.feature-icon.shop {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  box-shadow: 0 8px 20px rgba(103, 194, 58, 0.3);
}

.feature-icon.find {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
}

.feature-icon.nutrition {
  background: linear-gradient(135deg, #e6a23c 0%, #f0a020 100%);
  box-shadow: 0 8px 20px rgba(230, 162, 60, 0.3);
}

.feature-content {
  flex: 1;
}

.feature-content h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.feature-content p {
  font-size: 15px;
  color: #666;
  line-height: 1.6;
  margin: 0 0 16px 0;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-arrow {
  position: absolute;
  bottom: 24px;
  right: 24px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 20px;
  transition: all 0.3s ease;
}

.feature-card:hover .feature-arrow {
  background: #409eff;
  color: #fff;
  transform: translateX(4px);
}

.feature-card.shop:hover .feature-arrow {
  background: #67c23a;
}

.feature-card.nutrition:hover .feature-arrow {
  background: #e6a23c;
}

@media (max-width: 768px) {
  .discover-container {
    padding: 24px 16px;
  }

  .discover-header h1 {
    font-size: 36px;
  }

  .discover-header p {
    font-size: 16px;
  }

  .feature-cards {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .feature-card {
    padding: 24px;
  }

  .feature-icon {
    width: 60px;
    height: 60px;
    font-size: 30px;
  }

  .feature-content h3 {
    font-size: 20px;
  }
}
</style>
