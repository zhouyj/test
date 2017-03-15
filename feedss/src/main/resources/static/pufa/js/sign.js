//发送短信验证码
function sendSms(type){
	$.post("portal/user/sendSms", {
		verify : $("input[name=verify]").val(),
		mobile : $("input[name=mobile]").val(),
		type : type
	}).done(function(data) {
		if (data.code == 0) {
			$("#sign-msg").hide();
			alert("短信已发送，请等待接收。");
		} else {
			$("#sign-msg").text(data.msg).fadeIn();
		}
	}).error(function(xhr, textStatus, errorThrown) {
		console.log(xhr.responseText);
	});
}

$(function() {
	$('#form-signin').ajaxForm({
		beforeSubmit: function(){
			$(".pufa-submit").attr("disabled", true);
		},
		
		success: function(data){
			$("#sign-msg").hide();
			if (data.code == 0) {
				if(data.gotoSignup){
					//验证卡片成功后跳转
					location.href="./signupByUserPwd"
				}
				
				if(data.data.user.profileIsEmpty){
					location.href = "improveInfoPage";// 登录成功跳转补充信息
				}else{
					location.href = "/";// 登录成功跳转主页
				}
			} else {
				$("#sign-msg").text(data.msg).fadeIn();
				console.log(data)
			}
			$(".pufa-submit").attr("disabled", false);//恢复按钮
		}
	});
	
	// 协议同意后注册按钮才能点击
	$("#remember-me").click(function() {
		if ($("#remember-me").prop('checked')) {
			$("#submit-signup").removeAttr("disabled");
		} else {
			$("#submit-signup").attr("disabled", "disabled");
		}
	});

	// 注册
	$('#form-signup').ajaxForm({
		beforeSubmit: function(){
			$(".pufa-submit").attr("disabled", true);
		},
		success: function(data) {
			$("#sign-msg").hide();
			if (data.code == 0) {
				location.href = "improveInfoPage";//成功跳转到信息完善页面
			} else {
				$("#sign-msg").text(data.msg).fadeIn();
				console.log(data)
			}
			$(".pufa-submit").attr("disabled", false);//恢复按钮
		}
	});
	
	//表单提交后跳转到个人页
	$('#form-submit-to-profile').ajaxForm({
		beforeSubmit: function(){
			$(".pufa-submit").attr("disabled", true);
		},
		success: function(data) {
			$("#sign-msg").hide();
			if (data.code == 0) {
				alert(data.msg);
				location.href = "profile";//成功跳转到个人信息页
			} else {
				$("#sign-msg").text(data.msg).fadeIn();
				console.log(data)
			}
			$(".pufa-submit").attr("disabled", false);//恢复按钮
		}
	});
	
	//补充或编辑个人信息
	$("#form-improve-info").ajaxForm({
		beforeSubmit: function(){
			$(".pufa-submit").attr("disabled", true);
		},
		
		success: function(data) {
			$("#sign-msg").hide();
			if (data.code == 0) {
				if(data.gotoProfile){
					//字段标识是否第一次填写补充信息
					location.href = "./profile";//成功跳转到信息完善页面
				}else{
					location.href = "./signupFinish";//成功跳转到注册结束页
				}
				console.log(data)
			} else {
				$('html, body').animate({scrollTop:0}, 'slow');//回到顶部
				$("#sign-msg").text(data.msg).fadeIn();
				console.log(data)
			}
			$(".pufa-submit").attr("disabled", false);//恢复按钮
		}
	});

	// 获取手机验证码
	$("#btn-send-sms").click(function() {
		sendSms("REGISTER");
	});
});