<script setup>
defineProps({
  label: String,
  value: [String, Number],
  /** Element Plus 图标组件 */
  icon: { type: Object, required: true },
  gradient: String,
})
</script>

<template>
  <div class="stat-card" :style="{ background: gradient }">
    <div class="icon-wrap">
      <el-icon :size="28">
        <component :is="icon" />
      </el-icon>
    </div>
    <div class="meta">
      <div class="label">{{ label }}</div>
      <div class="value">{{ value }}</div>
    </div>
  </div>
</template>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 16px;
  color: #fff;
  box-shadow: 0 10px 28px rgba(79, 70, 229, 0.35);
  min-height: 96px;
}
.icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
}
.label {
  font-size: 13px;
  opacity: 0.92;
}
.value {
  font-size: 26px;
  font-weight: 800;
  margin-top: 4px;
  letter-spacing: 0.02em;
}
</style>
