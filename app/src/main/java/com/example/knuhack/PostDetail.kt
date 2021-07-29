package com.example.knuhack

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.knuhack.dto.ApiResult
import com.example.knuhack.dto.CommentForm
import com.example.knuhack.dto.ReplyForm
import com.example.knuhack.entity.Comment
import com.example.knuhack.entity.Reply
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PostDetail : AppCompatActivity() {
    val mContext  = this

    // user data
    var userId : Long = -1
    lateinit var userNickname : String

    // board data
    val items : ArrayList<CustomAdapter.AdapterItem> = ArrayList<CustomAdapter.AdapterItem>()
    lateinit var content: String
    lateinit var tile: String
    lateinit var author: String
    lateinit var category: String
    var boardId : Long = -1

    // view
    lateinit var listView: ListView

    // board
    lateinit var commentTextView : EditText
    lateinit var titleTextView : TextView
    lateinit var detailTextView : TextView
    lateinit var authorTextView: TextView
    lateinit var kindTextView : TextView
    lateinit var backButton : ImageButton
    // button
    lateinit var writeCommentBtn : ImageView
    lateinit var toAuthorBtn : ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        backButton= findViewById<ImageButton>(R.id.back_button1)
        backButton.setOnClickListener{
            finish()

        }

        setDataWithIntent()
        findViews()
        setClickListeners()

        getCommentList(boardId)
    }

    private fun setDataWithIntent() {
        boardId = intent.getLongExtra("boardId",0)
        userId = intent.getLongExtra("userId", -1)
        intent.getStringExtra("nickname")?.let { userNickname = it }
        
        
        intent.getStringExtra("content")?.let { content = it }  // Intent에서 Key를 email로 가지고 있는 값 가져오기
        intent.getStringExtra("title")?.let { title = it }
        intent.getStringExtra("author")?.let { author = it }
        intent.getStringExtra("category")?.let{category=it}

        val commentText = findViewById<EditText>(R.id.commentEdit) as EditText
        val text1 = findViewById<TextView>(R.id.postTitle) as TextView
        text1.setText(title)
        val text2 = findViewById<TextView>(R.id.detailContent) as TextView
        text2.setText(content)
        val text3 = findViewById<TextView>(R.id.writer_postDetail) as TextView
        text3.setText(author)

        val commentbtn = findViewById<ImageView>(R.id.writeCommentBtn) as ImageView

        commentbtn.setOnClickListener {
            val content = commentText.text.toString().trim { it <= ' ' }
            Log.i("가져온 텍스트 : ", content)
            if (userNickname != null) {
                writeComment(boardId, userNickname, content, commentText)
            }
        }
    }

    private fun findViews(){
        commentTextView = findViewById<EditText>(R.id.commentEdit) as EditText
        titleTextView = findViewById<TextView>(R.id.postTitle) as TextView
        titleTextView.setText(title)
        detailTextView= findViewById<TextView>(R.id.detailContent) as TextView
        detailTextView.setText(content)
        authorTextView = findViewById<TextView>(R.id.writer_postDetail) as TextView
        authorTextView.setText(author)
        kindTextView = findViewById<TextView>( R.id.board_kind) as TextView
        if(category=="FREE"){
            kindTextView.setText("자유게시판")
        }
        else if(category=="QNA"){
            kindTextView.setText("QnA")
        }
        else if(category=="TEAM"){
            kindTextView.setText("팀 프로젝트 게시판")
        }
        else if(category=="STUDY"){
            kindTextView.setText("스터디 게시판")
        }

        listView = findViewById<ListView>(R.id.commentlistview)
        writeCommentBtn = findViewById<ImageView>(R.id.writeCommentBtn)
        toAuthorBtn = findViewById<ImageButton>(R.id.menu_btn_postDetail)
        R.id.board_kind

    }

    private fun setClickListeners(){


        writeCommentBtn.setOnClickListener {
            val content = commentTextView.text.toString().trim { it <= ' ' }
            Log.i("가져온 텍스트 : ", content)
            if (userNickname != null) {
                writeComment(boardId, userNickname, content, commentTextView)
            }
        }

        toAuthorBtn.setOnClickListener {
            setAuthorDialog(author)
        }
    }

    private fun setAuthorDialog(author: String){
        val oItems = listOf<CharSequence>("프로필 보기", "쪽지 보내기").toTypedArray()
        val oDialog = AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        oDialog.setTitle("무엇을 하시겠어요?").setItems(oItems, DialogInterface.OnClickListener { dialog, which ->
            when(oItems[which]){
                oItems[0] -> startProfileActivity(author)
                oItems[1] -> sendMessage(author)
            }
        }).setCancelable(true).show();
    }

    private fun setCommentDialog(item : CustomAdapter.AdapterItem) {

        val oItems = listOf<CharSequence>("프로필 보기", "쪽지 보내기", "대댓글 작성", "수정", "삭제").toTypedArray()
        val oDialog = AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
        oDialog.setTitle("무엇을 하시겠어요?").setItems(oItems, DialogInterface.OnClickListener { dialog, which ->
            when(oItems[which]){
                oItems[0] -> startProfileActivity(item.author)
                oItems[1] -> sendMessage(item.author)
                oItems[2] -> writeReply(item.commentId, userNickname)
                oItems[3] -> edit(item.commentId, item.replyId, userNickname)
                oItems[4] -> delete(item.commentId, item.replyId)
            }

        }).setCancelable(true).show();
    }

    private fun sendMessage(toNickname:String){
        Toast.makeText(mContext, "send", Toast.LENGTH_LONG).show()
    }

    private fun startProfileActivity(nickname: String){
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("nickname", nickname)
        intent.putExtra("id", -1)
        startActivity (intent)
    }

    private fun writeReply(commentId: Long, nickname: String){ // TODO : commentId: Long 추가
        val dialogView = layoutInflater.inflate(R.layout.dialog_reply, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val replyContent = dialogView.findViewById<EditText>(R.id.reply_content).text
        val confirmBtn = dialogView.findViewById<Button>(R.id.writeReplyConfirmButton)
        val cancelBtn = dialogView.findViewById<Button>(R.id.writeReplyCancelButton)

        confirmBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("입력 확인", "답글 내용 : $replyContent")
            writeReply(commentId, nickname, replyContent.toString())
        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("답글 작성 취소 ", "답글 입력을 취소했습니다.")
        }

        alertDialog.show()
    }

    private fun edit(commentId: Long, replyId: Long, nickname: String){
        Toast.makeText(mContext, "edit", Toast.LENGTH_LONG).show()

        if(replyId > 0) {
            Log.e("REPLY " , "리플라이")
            val dialogView = layoutInflater.inflate(R.layout.dialog_reply_update, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            val confirmBtn = dialogView.findViewById<Button>(R.id.updateReplyConfirmButton)
            val cancelBtn = dialogView.findViewById<Button>(R.id.updateReplyCancelButton)

            confirmBtn.setOnClickListener {
                val updatedContent = dialogView.findViewById<EditText>(R.id.reply_update_content).text
                alertDialog.dismiss()
                Log.d("입력 확인", "수정 내용 : $updatedContent")
                updateReply(commentId, replyId, nickname, updatedContent.toString())
            }

            cancelBtn.setOnClickListener {
                alertDialog.dismiss()
                Log.d("답글 수정 취소 ", "답글 수정을 취소했습니다.")
            }
            alertDialog.show()
        } else if(commentId > 0){
            Log.e("COMMENT" , "코멘트")
            val dialogView = layoutInflater.inflate(R.layout.dialog_comment_update, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            val confirmBtn = dialogView.findViewById<Button>(R.id.updateCommentConfirmButton)
            val cancelBtn = dialogView.findViewById<Button>(R.id.updateCommentCancelButton)

            confirmBtn.setOnClickListener {
                val updatedContent = dialogView.findViewById<EditText>(R.id.comment_update_content).text
                alertDialog.dismiss()
                Log.d("입력 확인", "수정 내용 : $updatedContent")
                updateComment(commentId, replyId, nickname, updatedContent.toString())
            }

            cancelBtn.setOnClickListener {
                alertDialog.dismiss()
                Log.d("댓글 수정 취소 ", "댓글 수정을 취소했습니다.")
            }

            alertDialog.show()
        }
    }

    private fun updateComment(commentId: Long, replyId: Long, nickname: String, updatedContent: String) {
        RestApiService.instance.editComment(commentId, CommentForm(updatedContent, nickname)).enqueue(object : Callback<ApiResult<String>> {
            override fun onResponse(call: Call<ApiResult<String>>, response: Response<ApiResult<String>>) {
                response.body()?.let {
                    Log.e("it ?!   " , it.toString() + "   코멘트 : " + commentId + "  리플라이 : " + replyId )
                    if(!it.success.equals("true")) return
                    Log.e("댓글 수정이 성공적으로 수행되었습니다.", it.toString())
                    for (item in items) {
                        if(item.commentId == commentId && item.type.equals("COMMENT")){
                            item.author = nickname // 댓글이 삭제되는 경우 작성자와 내용이 모두 변함
                            item.content = updatedContent
                            break
                        }
                    }
                    val customAdapter = CustomAdapter(mContext, items)
                    listView.adapter = customAdapter
                }
            }

            override fun onFailure(call: Call<ApiResult<String>>, t: Throwable) {
                Toast.makeText(mContext,"댓글 수정에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                t.message?.let { Log.e("comment updating failed", it) }
            }
        })
    }


    private fun updateReply(commentId: Long, replyId : Long, nickname: String, updatedContent: String) {
        RestApiService.instance.editReply(replyId, ReplyForm(nickname, updatedContent)).enqueue(object : Callback<ApiResult<Reply>> {
            override fun onResponse(call: Call<ApiResult<Reply>>, response: Response<ApiResult<Reply>>) {
                response.body()?.let {
                    Log.i("댓글 수정이 성공적으로 수행되었습니다.", it.toString())
                    val customAdapter = CustomAdapter(mContext, items)
                    listView.adapter = customAdapter
                    customAdapter.changeOne(CustomAdapter.AdapterItem(nickname, updatedContent, "REPLY", commentId, replyId))
                }
            }

            override fun onFailure(call: Call<ApiResult<Reply>>, t: Throwable) {
                Toast.makeText(mContext,"댓글 작성에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                t.message?.let { Log.e("comment writing failed", it) }
            }
        })
    }



    class CustomAdapter(private val context: PostDetail, private val items: MutableList<AdapterItem>) : BaseAdapter() {
        data class AdapterItem(
            var author:String,
            var content:String,
            var type:String,
            var commentId : Long,
            var replyId : Long
        )

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): AdapterItem = items[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
            if(items[position].type.equals("COMMENT"))
            {
                val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.item_comment_list, null)
                val author = view.findViewById<TextView>(R.id.userNickname_comment)
                author.text = items[position].author
                val content = view.findViewById<TextView>(R.id.contents_comment)
                content.text = items[position].content

                view.findViewById<ImageButton>(R.id.menu_btn_comment).setOnClickListener {
                    context.setCommentDialog(items[position])
                }

                return view
            }
            else
            {
                val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.item_reply_list, null)
                val author = view.findViewById<TextView>(R.id.userNickname_reply)
                author.text = items[position].author
                val content = view.findViewById<TextView>(R.id.content_reply)
                content.text = items[position].content

                view.findViewById<ImageButton>(R.id.menu_btn_reply).setOnClickListener {
                    context.setCommentDialog(items[position])
                }

                return view
            }

        }

        fun addOne(adapterItem: AdapterItem){
            items.add(adapterItem)
            notifyDataSetChanged()
        }

        fun changeOne(adapterItem: AdapterItem){
            for(item in items){
                if(item.author.equals(adapterItem.author)){
                    item.content = adapterItem.content
                }
            }
        }
    }

    private fun getCommentList(boardId : Long){
        RestApiService.instance.findContentsByBoardId(boardId).enqueue(object : Callback<ApiResult<List<Comment>>>{
            override fun onResponse(call: Call<ApiResult<List<Comment>>>, response: Response<ApiResult<List<Comment>>>) {
                Log.e("get board list ", "success get comment")
                items.clear()
                response.body()?.let {
                    Log.e("get board list ", "success response body")


                    it.response?.let{
                        for(comment in it){
                            items.add(CustomAdapter.AdapterItem(comment.author, comment.content, "COMMENT", comment.commentId, -1))
                            for(reply in comment.replyDtoList){
                                items.add(CustomAdapter.AdapterItem(reply.author, reply.content, "REPLY", comment.commentId, reply.id))
                            }
                        }
                    }

                    //        어답터 설정
                    listView.adapter = CustomAdapter(mContext, items)
                }
            }

            override fun onFailure(call: Call<ApiResult<List<Comment>>>, t: Throwable) {
                Toast.makeText(mContext, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun writeComment(boardId: Long, nickname: String, content: String, commentText: EditText) {
        RestApiService.instance.writeComment(boardId, CommentForm(content, nickname)).enqueue(object : Callback<ApiResult<Comment>> {
            override fun onResponse(call: Call<ApiResult<Comment>>, response: Response<ApiResult<Comment>>) {
                response.body()?.let {
                    Log.i("댓글 작성이 성공적으로 수행되었습니다.", it.toString())
                    commentText.setText("")
                    val customAdapter = CustomAdapter(mContext, items)
                    listView.adapter = customAdapter
                    customAdapter.addOne(CustomAdapter.AdapterItem(nickname, content, "COMMENT", it.response.commentId, -1))
                }
            }

            override fun onFailure(call: Call<ApiResult<Comment>>, t: Throwable) {
                Toast.makeText(mContext,"댓글 작성에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                t.message?.let { Log.e("comment writing failed", it) }
            }
        })
    }

    private fun writeReply(commentId: Long, nickname: String, replyContent: String) {
        RestApiService.instance.writeReply(commentId, ReplyForm(nickname, replyContent)).enqueue(object : Callback<ApiResult<Reply>> {
            override fun onResponse(call: Call<ApiResult<Reply>>, response: Response<ApiResult<Reply>>) {
                response.body()?.let {
                    Log.i("[Response] ", it.toString())
                    val customAdapter = CustomAdapter(mContext, items)
                    listView.adapter = customAdapter
                    customAdapter.addOne(CustomAdapter.AdapterItem(nickname, replyContent, "REPLY", commentId, 0)) // it.response.id
                }
            }

            override fun onFailure(call: Call<ApiResult<Reply>>, t: Throwable) {
                Toast.makeText(mContext,"답글 작성에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                t.message?.let { Log.e("reply writing failed", it) }
            }
        })
    }
    private fun delete(commentId: Long, replyId: Long){
        Toast.makeText(mContext, "delete", Toast.LENGTH_LONG).show()
        Log.e("커멘트와 리플라이 아이디 : ", commentId.toString() + " " + replyId)
        var target : Long
        if(replyId > 0L) {

            RestApiService.instance.deleteReply(replyId).enqueue(object : Callback<ApiResult<String>> {
                override fun onResponse(call: Call<ApiResult<String>>, response: Response<ApiResult<String>>) {
                    response.body()?.let {
                        Log.e("[RESPONSE] ", it.toString())
                        val customAdapter = CustomAdapter(mContext, items)
                        listView.adapter = customAdapter
                    }
                }

                override fun onFailure(call: Call<ApiResult<String>>, t: Throwable) {
                    Toast.makeText(mContext, "답글 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    t.message?.let { Log.e("reply deleting failed", it) }
                }
            })
        } else if(commentId > 0)
            RestApiService.instance.deleteComment(commentId).enqueue(object : Callback<ApiResult<Boolean>> {
                override fun onResponse(call: Call<ApiResult<Boolean>>, response: Response<ApiResult<Boolean>>) {
                    response.body()?.let {
                        Log.e("[RESPONSE] ", it.toString())
                        updateComment(commentId, -1, "알 수 없음", "삭제된 댓글입니다.")
                        val customAdapter = CustomAdapter(mContext, items)
                        listView.adapter = customAdapter
                    }
                }

                override fun onFailure(call: Call<ApiResult<Boolean>>, t: Throwable) {
                    Toast.makeText(mContext, "댓글 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    t.message?.let { Log.e("comment deleting failed", it) }
                }
            })
    }

}