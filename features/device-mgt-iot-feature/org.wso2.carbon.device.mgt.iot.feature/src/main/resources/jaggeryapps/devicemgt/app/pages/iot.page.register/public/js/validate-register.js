$(document).ready(function(){
    $("#registerForm").validate({
        onfocusout: false,
        rules: {
            first_name: {
                required: true
            },
            last_name: {
                required: true
            },
            user_name: {
                required: true,
                minlength: 3
            },
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                minlength: 5
            },
            password_confirmation: {
                required: true,
                equalTo: "#password"
            }
        },
        messages: {
            first_name: {
                required: "Firstname is a required field. This cannot be empty."
            },
            last_name: {
                required: "Lastname is a required field. This cannot be empty."
            },
            user_name: {
                required: "Username is a required field. This cannot be empty.",
                minlength: ""
            },
            email: {
                required: "Email is a required field. This cannot be empty.",
                email: "Email is not valid. Please enter a correct email address."
            },
            password: {
                required: "Please enter a user login password",
                minlength: "Password should be between 5 and 30 characters."
            },
            password_confirmation: {
                required: "Please enter a user login password",
                equalTo: "Please enter the same password as above"
            }
        },
        submitHandler: function(form) {
            var addUserFormData = {};
            addUserFormData.username = $("input#user_name").val();
            addUserFormData.firstname = $("input#first_name").val();
            addUserFormData.lastname = $("input#last_name").val();
            addUserFormData.emailAddress = $("input#email").val();
            addUserFormData.password = $("input#password").val();
            addUserFormData.userRoles = null;

            var context = $(".form-login-box").attr("action");
            var addUserAPI = context + "/api/user/register";

            $.ajax({
                type: 'POST',
                url: addUserAPI,
                contentType: 'application/json',
                data: JSON.stringify(addUserFormData),
                success: function (data) {
                    if (data == 200) {
                        $('.wr-validation-summary strong').html("<i class=\"icon fw fw-ok\"></i> Successfully Submitted.");
                        $('.wr-validation-summary').removeClass("alert-danger");
                        $('.wr-validation-summary').addClass("alert-success");
                    } else if (data == 201) {
                        $('.wr-validation-summary strong').html("<i class=\"icon fw fw-ok\"></i> User created " +
                            "succssfully. You will be redirected to login page.");
                        $('.wr-validation-summary').removeClass("alert-danger");
                        $('.wr-validation-summary').addClass("alert-success");
                        setTimeout(function () {
                            window.location = context + "/login";
                        }, 2000);
                    } else if (data == 400) {
                        $('.wr-validation-summary strong').html("<i class=\"icon fw fw-error\"></i> Exception at backend.");
                        $('.wr-validation-summary').removeClass("alert-danger");
                        $('.wr-validation-summary').addClass("alert-warning");
                    } else if (data == 403) {
                        $('.wr-validation-summary strong').html("Action not permitted.");
                    } else if (data == 409) {
                        $('.wr-validation-summary strong').html("<i class=\"icon fw fw-info\"></i> User name already exists.");
                        $('.wr-validation-summary').removeClass("alert-default");
                        $('.wr-validation-summary').addClass("alert-success");
                    }
                    $('.wr-validation-summary').removeClass("hidden");
                    $('#password').val('');
                    $('#password_confirmation').val('');
                },
                error: function (err) {
                    $('.wr-validation-summary strong').html("<i class=\"icon fw fw-error\"></i> An unexpected error occurred.");
                    $('.wr-validation-summary').removeClass("hidden");
                    return false;
                }
            });
        }

    });
});