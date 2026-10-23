<template>
    <div class="page-container fade-up">
        <!-- Hero -->
        <div class="about-hero soft-card">
            <div class="about-copy">
                <h1 class="about-title">关于我们</h1>
                <p class="about-slogan">让流浪止于关爱，让陪伴不再缺席。</p>
                <p class="about-text">
                    「流浪猫狗之家」是一个面向社会的公益救助平台。我们为流浪动物提供信息发布、
                    领养对接、物资捐助与社区交流的一站式服务，帮助更多小生命找到温暖的归宿。
                </p>
                <p class="about-text">
                    平台由一群热爱动物的志愿者共同维护。我们相信，每一次领养、每一份捐助、
                    每一条转发，都会让这个世界对小动物更友善一点。
                </p>
            </div>
            <div class="about-image">
                <img src="/logo.png" alt="About Us Image">
            </div>
        </div>
        <!-- 三个理念 -->
        <div class="value-grid">
            <div class="value-card soft-card">
                <span class="value-icon">🏠</span>
                <h3 class="value-title">领养代替购买</h3>
                <p class="value-desc">为待回家的猫狗与爱心人士搭建可信的领养桥梁，全程透明可追踪。</p>
            </div>
            <div class="value-card soft-card">
                <span class="value-icon">💝</span>
                <h3 class="value-title">爱心救助</h3>
                <p class="value-desc">捐助物资直达救助一线，每一笔爱心都有记录、有回响。</p>
            </div>
            <div class="value-card soft-card">
                <span class="value-icon">🤝</span>
                <h3 class="value-title">社区互助</h3>
                <p class="value-desc">分享救助故事与养宠经验，让更多人参与到流浪动物保护中来。</p>
            </div>
        </div>
    </div>
</template>

<script setup>


</script>
<style scoped>
/* ===== Hero ===== */
.about-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 40px;
    padding: 44px 48px;
    margin-bottom: 26px;
    background:
        radial-gradient(420px 240px at 92% 10%, rgba(255, 138, 80, .12) 0%, transparent 60%),
        #fff;
}

.about-copy {
    flex: 1;
    min-width: 0;
}

.about-title {
    margin: 0 0 8px;
    font-size: 34px;
    color: var(--ink-1);
    letter-spacing: 1px;
}

.about-slogan {
    margin: 0 0 20px;
    font-size: 17px;
    font-weight: 600;
    background: var(--brand-gradient);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
}

.about-text {
    color: var(--ink-2);
    line-height: 1.9;
    font-size: 15px;
    margin: 0 0 12px;
}

.about-image {
    flex-shrink: 0;
}

.about-image img {
    width: 260px;
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow);
    animation: fade-in .8s ease;
}

@keyframes fade-in {
    from {
        opacity: 0;
        transform: translateY(10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* ===== 理念三卡 ===== */
.value-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
}

.value-card {
    padding: 28px 24px;
    text-align: center;
}

.value-card:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow);
}

.value-icon {
    font-size: 34px;
    display: block;
    margin-bottom: 14px;
}

.value-title {
    margin: 0 0 10px;
    font-size: 17px;
    color: var(--ink-1);
}

.value-desc {
    margin: 0;
    font-size: 13px;
    color: var(--ink-2);
    line-height: 1.8;
}

/* ===== 响应式 ===== */
@media (max-width: 860px) {
    .about-hero {
        flex-direction: column;
        padding: 30px 24px;
        text-align: center;
    }

    .about-image img {
        width: 200px;
    }

    .value-grid {
        grid-template-columns: 1fr;
    }
}
</style>
