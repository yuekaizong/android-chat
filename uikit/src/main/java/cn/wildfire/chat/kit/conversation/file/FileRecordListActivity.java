package cn.wildfire.chat.kit.conversation.file;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import butterknife.OnClick;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.contact.pick.PickContactActivity;
import cn.wildfire.chat.kit.conversation.pick.PickConversationActivity;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.UserInfo;

public class FileRecordListActivity extends WfcBaseActivity {
    private static final int PICK_CONVERSATION_REQUEST = 200;
    private static final int PICK_CONTACT_REQUEST = 201;

    @Override
    protected int contentLayout() {
        return R.layout.activity_file_record_list;
    }

    @Override
    protected void afterViews() {
        if (!isDarkTheme()) {
            setTitleBackgroundResource(R.color.white, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R2.id.allFilesItemView)
    void allFiles() {
        Intent intent = new Intent(this, FileRecordActivity.class);
        startActivity(intent);
    }

    @OnClick(R2.id.myFilesItemView)
    void myFiles() {
        Intent intent = new Intent(this, FileRecordActivity.class);
        intent.putExtra("isMyFiles", true);
        startActivity(intent);
    }

    @OnClick(R2.id.conversationFilesItemView)
    void convFiles() {
        Intent intent = new Intent(this, PickConversationActivity.class);
        startActivityForResult(intent, PICK_CONVERSATION_REQUEST);
    }

    @OnClick(R2.id.userFilesItemView)
    void userFiles() {
        //Todo Select a user first.
        Intent intent = new Intent(this, PickContactActivity.class);
        intent.putExtra("showChannel", false);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == PICK_CONVERSATION_REQUEST) {
            ConversationInfo conversationInfo = data.getParcelableExtra("conversationInfo");
            if (conversationInfo != null) {
                Intent intent = new Intent(this, FileRecordActivity.class);
                intent.putExtra("conversation", conversationInfo.conversation);
                startActivity(intent);
            }
        } else if (requestCode == PICK_CONTACT_REQUEST) {
            UserInfo userInfo = data.getParcelableExtra("userInfo");
            if (userInfo != null) {
                Intent intent = new Intent(this, FileRecordActivity.class);
                intent.putExtra("fromUser", userInfo.uid);
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}