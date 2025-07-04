<template>
    <div class="about-us">
        <h2 class="section-title">关于我们</h2>
        <div class="about-content">
            <div class="about-description">
                <h3 class="about-title">我们的使命</h3>
                <p class="about-text">
                    Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                    Nunc sit amet gravida tellus. Vivamus et nulla auctor,
                    volutpat magna ac, tempor neque. Proin a semper dolor,
                    id ultrices arcu. Donec aliquet mauris vitae mi consectetur,
                    non tincidunt risus consectetur.
                </p>
            </div>
            <div class="about-image">
                <img src="logo.png" alt="About Us Image">
            </div>
        </div>
    </div>
</template>

<script setup>


</script>
<style scoped>
.about-us {
    padding: 50px;
}

.section-title {
    text-align: center;
    font-size: 36px;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
}

.about-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 40px;
}

.about-description {
    flex: 1;
    margin-right: 40px;
}

.about-title {
    font-size: 24px;
    font-weight: bold;
    color: #555;
    margin-bottom: 10px;
}

.about-text {
    color: #777;
    line-height: 1.5;
}

.about-image img {
    width: 400px;
    border-radius: 5px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    animation: fade-in 1s;
}

@keyframes fade-in {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}
</style>