//package com.issyzone.syzbleprinter
//
//import android.content.Intent
//import android.net.Uri
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import com.issyzone.syzbleprinter.databinding.ActivityMain3Binding
//import com.issyzone.syzbleprinter.databinding.ActivityMain6Binding
//import com.issyzone.syzbleprinter.utils.invokeViewBinding
//import com.issyzone.syzlivechat.SyzLiveChatUtils
//import com.livechatinc.inappchat.ChatWindowErrorType
//import com.livechatinc.inappchat.ChatWindowEventsListener
//import com.livechatinc.inappchat.models.NewMessageModel
//
//class Main6Activity : ComponentActivity(), ChatWindowEventsListener {
//    private val vm: ActivityMain6Binding by invokeViewBinding()
//    private val TAG="MAIN6>>>>>"
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(vm.root)
//        test()
//
//    }
//
//    fun test(){
//        vm.embeddedChatWindow.setConfiguration(SyzLiveChatUtils.initLiveDataConfigrations("17778096"))
//        vm.embeddedChatWindow.setEventsListener(this)
//        vm.embeddedChatWindow.initialize()
//
//    }
//
//    override fun onWindowInitialized() {
//        Log.i(TAG,">>>onWindowInitialized")
//        vm.embeddedChatWindow.showChatWindow()
//    }
//
//    override fun onChatWindowVisibilityChanged(visible: Boolean) {
//        Log.i(TAG,">>>onChatWindowVisibilityChanged===${visible}")
//    }
//
//    override fun onNewMessage(message: NewMessageModel?, windowVisible: Boolean) {
//        Log.i(TAG,">>>onNewMessage>>>${message.toString()}")
//    }
//
//    override fun onStartFilePickerActivity(intent: Intent?, requestCode: Int) {
//        Log.i(TAG,">>>onStartFilePickerActivity>>>${requestCode.toString()}")
//    }
//
//    override fun onRequestAudioPermissions(permissions: Array<out String>?, requestCode: Int) {
//        Log.i(TAG,">>>onRequestAudioPermissions>>>${permissions.toString()}")
//    }
//
//    override fun onError(
//        errorType: ChatWindowErrorType?,
//        errorCode: Int,
//        errorDescription: String?
//    ): Boolean {
//        Log.i(TAG,">>>onError==${errorDescription}")
//        return false
//    }
//
//    override fun handleUri(uri: Uri?): Boolean {
//        Log.i(TAG,">>>handleUri===${uri}")
//        return false
//
//    }
//}