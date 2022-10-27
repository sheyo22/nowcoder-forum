function like(btn,type,id,userId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":type,"entityId":id,"entityUserId":userId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            $(btn).children("i").text(data.likeCount);
            $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
        }
    );
    
}