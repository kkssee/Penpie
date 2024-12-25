$(document).ready(function() {
    // 페이지 로드 시 쿠키에서 사이드바 상태를 읽어와서 설정
    if (getCookie('sidebarStatus') === 'on') {
        $(".sidebar").addClass("active");
        $("body").addClass("active");
    } else {
        $(".sidebar").removeClass("active");
        $("body").removeClass("active");
    }

    // transition을 없앤 후 설정 완료 후 transition을 활성화
    setTimeout(function() {
        $(".sidebar, body").css("transition", "all 0.3s ease");
    }, 50); // 약간의 시간 차를 두고 transition을 활성화

    $(".menu-btn").click(function () {
        $(".sidebar").toggleClass("active");
        $("body").toggleClass("active");

        if ($(".sidebar").hasClass("active")) {
            document.cookie = "sidebarStatus=on; path=/";
        } else {
            document.cookie = "sidebarStatus=off; path=/";
        }

        $(".menu > ul > li").removeClass("active");
        $(".menu .sub-menu").hide();
    });

    $(".menu > ul > li").click(function () {
        $(this).siblings().removeClass("active");
        $(this).toggleClass("active");
        $(this).find("ul").slideToggle();
        $(this).siblings().find("ul").slideUp();
        $(this).siblings().find("ul").find("li").removeClass("active");
    });

    function getCookie(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    }
});


document.addEventListener('DOMContentLoaded', () => {
    console.log("테마가져오긔");
    const savedTheme = localStorage.getItem('theme');
    console.log(savedTheme);
    applyTheme(savedTheme || 'system');
});

// 테마 설정 함수
function setTheme(theme) {
    localStorage.setItem('theme', theme); // 로컬 저장소에 테마 저장
    applyTheme(theme);
    applyHeaderTheme(theme);
}

// 테마 적용 함수
function applyTheme(theme) {
    const root = document.documentElement;
    root.classList.remove('light-theme', 'dark-theme');

    if (theme === 'light') {
        root.classList.add('light-theme');
    } else if (theme === 'dark') {
        root.classList.add('dark-theme');
    } else {
        // 시스템 기본 설정 사용
        const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        root.classList.toggle('dark-theme', systemPrefersDark);
        root.classList.toggle('light-theme', !systemPrefersDark);
    }
}