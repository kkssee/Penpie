$('.btn-example').click(function(){
    const $href = $(this).attr('href');
    layer_popup($href);
});
function layer_popup(el){

    const $el = $(el);    //레이어의 id를 $el 변수에 저장
    const isDim = $el.prev().hasClass('dimBg'); //dimmed 레이어를 감지하기 위한 boolean 변수

    isDim ? $('.dim-layer').fadeIn() : $el.fadeIn();

    const $elWidth = ~~($el.outerWidth()),
        $elHeight = ~~($el.outerHeight()),
        docWidth = $(document).width(),
        docHeight = $(document).height();

    // 화면의 중앙에 레이어를 띄운다.
    if ($elHeight < docHeight || $elWidth < docWidth) {
        $el.css({
            marginTop: -$elHeight /2,
            marginLeft: -$elWidth/2
        })
    } else {
        $el.css({top: 0, left: 0});
    }

    $el.find('a.btn-layerClose').click(function(){
        isDim ? $('.dim-layer').fadeOut() : $el.fadeOut(); // 닫기 버튼을 클릭하면 레이어가 닫힌다.
        return false;
    });

    $('.layer .dimBg').click(function(){
        $('.dim-layer').fadeOut();
        return false;
    });

}

document.getElementById("characterVisibilityToggle").addEventListener("change", function(event) {
    const isChecked = event.target.checked;
    const visibilityInput = document.getElementById("characterVisibility");
    const visibilityStatus = document.getElementById("visibilityStatus");

    // Set hidden input value based on checkbox state
    visibilityInput.value = isChecked ? "1" : "0";

    visibilityStatus.innerHTML = isChecked
        ? "Private"
        : "Public<br>Once set to public, you cannot change it back to private.";
});

$(document).ready(function() {
    // File type validation and preview
    $('#file').on('change', function () {
        validateFileType(this);
    });

    function validateFileType(input) {
        var file = input.files[0];
        var allowedExtensions = ['jpg', 'jpeg', 'png', 'gif'];
        var fileExtension = file.name.split('.').pop().toLowerCase();

        if ($.inArray(fileExtension, allowedExtensions) === -1) {
            alert('올바르지 않은 파일 형식입니다. jpg, jpeg, png, gif 파일만 가능합니다.');
            input.value = '';  // 파일 입력 초기화
        } else {
            previewImage(input);
        }
    }
    function previewImage(input) {
        const file = input.files[0];
        const reader = new FileReader();

        reader.onload = function(e) {
            $('#imagePreview').attr('src', e.target.result).show();  // 이미지 미리보기
        }

        reader.readAsDataURL(file);
    }
});

function createCharacter() {
    const form = $('#characterCreateForm')[0];
    const data = new FormData(form);

    $('#createCharacterBtn').prop('disabled', true);
    $.ajax({
        enctype: 'multipart/form-data',
        url : '/character/create',
        method : 'POST',
        cache : false,
        data : data,
        dataType : 'json',
        processData: false,
        contentType: false,
        timeout: 600000,
        success : function(res) {
            if(res.created) {
                alert('생성 성공');
                location.href = '';
            } else {
                alert('생성 실패');
            }
        },
        error : function(xhr, status, err) {
            alert('create err: ' + err);
            $('#createCharacterBtn').prop('disabled', false);
        }
    });
}
