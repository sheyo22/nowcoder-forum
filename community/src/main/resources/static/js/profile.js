$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3,"entityId":$("#entity-id").val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code = 200) window.location.reload();
				else alert(data.msg);
			}
		);
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3,"entityId":$("#entity-id").val()},
			function (data) {
				data = $.parseJSON(data);
				if(data.code = 200) window.location.reload();
				else alert(data.msg);
			}
		);
	}
}