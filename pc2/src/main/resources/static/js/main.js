//Клик по любой ссылке в табах
//Скрывает все вкладки и удаляет выделения во на всех ссылках
$('.tabs-menu a').click(function (e) {
    $('.tabs-menu a').removeClass('active');
    $('#containerOfTabs>div').hide();
    e.preventDefault();
});

$(document).ready(function () {

    $('#linkShowSubmitionsTeacher').click(function(){
        getSubmitions();
    });

    $('#linkShowSubmitionsStudent').click(function(){
        getSubmitions();
    });

    $('#sourceTM2').hide();

    $('#radioSourceFileSubmit').click(function (e) {
        $('#file').prop('disabled', false);
        $('#codeSolutionSend').prop('disabled', true);
        $('#flagSourceCodeNo').val(false);
    });

    $('#radioSourceCodeSubmit').click(function (e) {
        $('#file').prop('disabled', true)
        $('#codeSolutionSend').prop('disabled', false);
        $('#flagSourceCodeNo').val(true);
    });


    $('#radioSourceFilePerfect').click(function (e) {
        $('#filePerfect').prop('disabled', false);
        $('#codeSolutionPerfect').prop('disabled', true);
        $('#flagSourceCodePerfectNo').val(false);
    });

    $('#radioSourceCodePerfect').click(function (e) {
        $('#filePerfect').prop('disabled', true)
        $('#codeSolutionPerfect').prop('disabled', false);
        $('#flagSourceCodePerfectNo').val(true);
    });


    $("#submit").click(function () {
        $("#form").submit();
    });

    $("#curUserName").ajaxComplete($.ajax({
        type: 'get',
        url: "/username",
        data: {},
        dataType: "html",
        success: function (data) {
            $("#curUserName").html(data);
            console.log("name = " + data);
        }
    }));

    $("#downloadLoginPassword").click(function () {
        var groupId = $('#groupId').val();
        $.ajax({
            type : 'get',
            url: '/download/b',
            dataType: 'binary',
            data : {'groupId' : groupId},
            xhrFields: {
                'responseType': 'blob'
            },
            success: function (data, status, xhr) {
                var blob = new Blob([data], {type: xhr.getResponseHeader('Content-Type')});
                var link = document.createElement('a');
                link.href = window.URL.createObjectURL(blob);
                link.download = 'report.pdf';
                link.click();
            }
        })
    });



    $('#sourceFile').click(function(e){
        $('#sourceTM1').show();
        $('#sourceTM2').hide();
        $('#source-text2').val("");
    });

    $('#sourceLink').click(function(e) {
        $('#sourceTM1').hide();
        $('#sourceTM2').show();
    });
   /* $("#loadTheoryFile").click(function () {
        $.ajax({
            type: 'post',
            url: "/teacher/course/"+courseId+"/chapter/"+chapterId+"/add-theory",
            data: {'theoryMaterial': theoryMaterial },
            dataType: "html",
            success: function () {
                console.log("файл теории был успешно загружен");
            }
        });
    };*/
    //При клике на чекбокс тега помечаются или наоборот снимаются галки со всех его потомков
    //todo: есть косяки при рекурсивном запуске события, но пока так
    $(".tag-checkbox").click(function (e) {
        var idTag = $(this).attr('id').split('-')[1];
        var classChild = '.parent-' + idTag;
        $(classChild).click();
        $(classChild).prop('checked', $(this).is(':checked'));
    });

    $('#radioSourceFileSubmit').click(function (e) {
        $('#file').prop('disabled', false);
        $('#codeSolutionSend').prop('disabled', true);
        $('#flagSourceCodeNo').val(false);
    });

    $('#radioSourceCodeSubmit').click(function (e) {
        $('#file').prop('disabled', true)
        $('#codeSolutionSend').prop('disabled', false);
        $('#flagSourceCodeNo').val(true);
    });

});


function loadFileByPath(nameOfFile) {
    var arrayPath = nameOfFile.split("\\");
    var filename = arrayPath[arrayPath.length - 1];
    $.ajax({
        type : 'get',
        url: '/download/nameOfFile',
        data : {'nameOfFile' : nameOfFile},
        dataType: 'binary',
        xhrFields: {
            'responseType': 'blob'
        },
        success: function (data, status, xhr) {
            var blob = new Blob([data], {type: xhr.getResponseHeader('Content-Type')});
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = filename;
            link.click();
        }
    })
}

function editFioTeacher (idTeacher, stringFIO) {
     $.ajax({
         type: 'post',
         url: "/admin/editTeacher/",
         data: {'fio': stringFIO, 'idTeacher': idTeacher},
         dataType: "html",
         success: function (data) {
             console.log("фио было успешно изменено " + data);
         }
     });
 }

 function editFioStudent (idStudent, stringFIO) {
     $.ajax({
         type: 'post',
         url: "/teacher/editStudent/",
         data: {'fio': stringFIO, 'idStudent': idStudent},
         dataType: "html",
         success: function (data) {
             console.log("фио было успешно изменено " + data);
         }
     });
 }

function getSubmitions() {
    $.ajax({
        type: 'post',
        url: "/user/submitions",
        dataType: "html",
        success: function (data) {
            $("#tableSubmitions").html(data);
            $('#ViewRunsUser').DataTable();
            $('.dataTables_length').addClass('bs-select');
            var dt = new Date();
            var time = dt.getHours() + ":" + dt.getMinutes() + ":" + dt.getSeconds();
            $("#timeLastRefreshTableSubmitions").text(time);
        }
    });
}

$('#refreshTableSubmitions').click(function (e) {
    getSubmitions();
});

$(".myButtonFio").click(function (e) {
    var editBtn = $(this);                              //Кнопка с редактированием
    var itemDivTeach = editBtn.parent().parent();     //Элемент списка с преподом
    var editInputFIO = itemDivTeach.find(".editInputFIO");  //Инпут для редактирования
    var textFIO = itemDivTeach.find(".textFIO");

    if (editBtn.hasClass("myButtonFioEdit")) {      //Завершаем редактирование
        var stringFIO = editInputFIO.val().trim();

        var invalidDiv = itemDivTeach.find(".invalid-tooltip");
        if (stringFIO.length == 0) {
            invalidDiv.text("ФИО не должно быть пустым");
            invalidDiv.show(200);
        } else if (stringFIO.split(/\s+/).length != 3) {
            invalidDiv.text("ФИО некорректно");
            invalidDiv.show(200);
        } else {
            invalidDiv.hide();
            var idUser = itemDivTeach.attr("id").split('_')[1];
            var whoIs = itemDivTeach.attr("id").split('_')[0];

            //чтобы определить, кого изменять, преподавателя или студента
            if (whoIs == 'student') {
                editFioStudent(idUser, stringFIO);
            } else if (whoIs == 'teacher') {
                editFioTeacher(idUser, stringFIO);
            }


            textFIO.html('<i class="fa fa-user" aria-hidden="true"></i>&nbsp;' + stringFIO);

            editInputFIO.hide();
            textFIO.show(200);
            editBtn.html('<i class="fa fa-pencil fa-fw"></i>');
            editBtn.removeClass("myButtonFioEdit");
            editBtn.removeClass("btn-success");
            editBtn.addClass("btn-dark");
            itemDivTeach.removeClass("editable-div-item");
        }
    } else {        //Начинаем редактирование

        textFIO.hide();
        editInputFIO.show(400);
        editBtn.html('<i class="fa fa-floppy-o" aria-hidden="true"></i>');
        editBtn.addClass("myButtonFioEdit");
        editBtn.removeClass("btn-dark");
        editBtn.addClass("btn-success");
        itemDivTeach.addClass("editable-div-item");
    }

     e.preventDefault();
});






//todo: такая себе валидация, надо будет сделать нормальную
//Копипаст с документации bootstrap для валидации
//https://getbootstrap.com/docs/4.0/components/forms/
// Example starter JavaScript for disabling form submissions if there are invalid fields
  window.addEventListener('load', function() {
    // Fetch all the forms we want to apply custom Bootstrap validation styles to
    var forms = document.getElementsByClassName('needs-validation');
    // Loop over them and prevent submission
    var validation = Array.prototype.filter.call(forms, function(form) {
      form.addEventListener('submit', function(event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  }, false);
