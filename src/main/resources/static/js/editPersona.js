function editPersona() {
    const form = $('#editPersonaForm')[0];
    const data = new FormData(form);

    $('#editPersonaBtn').prop('disabled', true);
    $.ajax({
        enctype: 'multipart/form-data',
        url : '/user/persona',
        method : 'POST',
        cache : false,
        data : data,
        dataType : 'json',
        processData: false,
        contentType: false,
        timeout: 600000,
        success : function(res) {
            if(res.edited) {
                alert('수정 성공');
                location.href = '';
            } else {
                alert('수정 실패');
            }
        },
        error : function(xhr, status, err) {
            alert('create err: ' + err);
            $('#editPersonaBtn').prop('disabled', false);
        }
    });
}
