package com.issyzone.syzlivechat

import android.view.View
import com.livechatinc.inappchat.ChatWindowConfiguration
import com.livechatinc.inappchat.ChatWindowViewImpl


object SyzLiveChatUtils {
     fun initLiveDataConfigrations(licenceNr: String): ChatWindowConfiguration {
        return ChatWindowConfiguration.Builder()
            .setLicenceNumber(licenceNr)
           // .setGroupId(groupId)
            .setVisitorName("hcy")
            .setVisitorEmail("384317693@qq.com")
            .build()
    }



//    fun startFullScreenChat(activity: Activity,licenceNr: String, groupId: String,visitorName:String,visitorEmail:String) {
//      val  configuration= ChatWindowConfiguration.Builder().setLicenceNumber(licenceNr).setGroupId(groupId)
//            .setVisitorName(visitorName).setVisitorEmail(visitorEmail).build()
//        if (fullScreenChatWindow == null) {
//            fullScreenChatWindow = ChatWindowUtils.createAndAttachChatWindowInstance(activity)
//            fullScreenChatWindow.setConfiguration(configuration)
//            fullScreenChatWindow.setEventsListener(this)
//            fullScreenChatWindow.initialize()
//        }
//        fullScreenChatWindow.showChatWindow()
//    }


}