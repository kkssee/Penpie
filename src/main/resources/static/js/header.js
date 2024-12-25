document.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('theme');
    applyHeaderTheme(savedTheme || 'system');
});

// 테마에 따른 로고 이미지 경로
const logoImages = {
    light: '/img/logo/penpie_lightmode.png',
    dark: '/img/logo/penpie_darkmode.png'
};

// 헤더에만 적용되는 테마 설정 함수
function applyHeaderTheme(theme) {
    const logo = document.getElementById('logo-image');

    if (theme === 'light') {
        logo.src = logoImages.light;
    } else if (theme === 'dark') {
        logo.src = logoImages.dark;
    } else {
        // 시스템 기본 설정 사용
        const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        logo.src = systemPrefersDark ? logoImages.dark : logoImages.light;
    }
}

