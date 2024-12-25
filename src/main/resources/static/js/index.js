function fetchComments() {
    $.ajax({
        url: '/index/comments', // 댓글을 가져올 API URL
        method: 'GET',
        success: function(data) {
            $('#commentsContainer').empty(); // 기존 댓글 삭제
            data.forEach(comment => {
                const replyButton = comment.parentCommentId !== 0 ?
                    `<button type="button" onclick="window.location.href='/novel/reply/${comment.parentCommentId}#comment-${comment.commentId}'">Go to the reply</button>` :
                    `<button type="button" onclick="window.location.href='/novel/comments/${comment.epId}#comment-${comment.commentId}'">Go to the comment</button>`;

                $('#commentsContainer').append(`
            <div class="comment" id="comment-${comment.commentId}">
                <strong>${comment.nickname}</strong>
                <p>${comment.commentContents}</p>
                <p>좋아요 개수: ${comment.likeCount}</p>
                ${replyButton}
            </div>
        `);
            });
        },
        error: function(err) {
            console.error('Failed to get comments', err);
        }
    });
}

$(document).ready(function() {
    $('.slides').slick({
        centerMode: true,
        centerPadding: '100px',
        dots:true,
        arrows : true, 		// 옆으로 이동하는 화살표 표시 여부
        slidesToShow: 1,               // 한 화면에 보여질 컨텐츠 개수
        autoplay: true,               // 자동 슬라이드
        autoplaySpeed: 3000,          // 자동슬라이드 (in milliseconds)
        prevArrow: '<button class="slick-prev">◀</button>', // 이전 버튼
        nextArrow: '<button class="slick-next">▶</button>', // 다음 버튼
        responsive: [
            {
                breakpoint: 768,    //화면 사이즈 768px
                settings: {
                    arrows: false,
                    centerMode: true,
                    centerPadding: '20px',
                    slidesToShow: 1
                }
            },
            {
                breakpoint: 480,
                settings: {
                    arrows: false,
                    centerMode: true,
                    centerPadding: '10px',
                    slidesToShow: 1
                }
            }
        ]
    });
    $('.slides').on('mouseleave', function() {  //마우스 벗어나고 다시 시작, 이거 안하면 너무 오래걸림
        $(this).slick('slickPlay');
    });
    fetchComments(); // 페이지 로드 시 댓글 가져오기
    setInterval(fetchComments, 60000); // 60초마다 댓글 업데이트
});
var g_webSocket = null;
window.onload = function() {
    setupWebSocket();
};
function setupWebSocket() {
    const host = "localhost"; // 배포 시 호스트 주소로 변경
    const url = "ws://" + host + "/ws/chat";

    if (g_webSocket == null || g_webSocket.readyState !== WebSocket.OPEN) {
        g_webSocket = new WebSocket(url);
    }

    g_webSocket.onopen = function() {
        console.log("WebSocket connection established.");
    };
}