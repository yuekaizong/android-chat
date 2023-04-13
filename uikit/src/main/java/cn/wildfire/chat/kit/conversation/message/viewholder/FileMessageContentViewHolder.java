/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.message.viewholder;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.annotation.MessageContentType;
import cn.wildfire.chat.kit.annotation.MessageContextMenuItem;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.message.model.UiMessage;
import cn.wildfire.chat.kit.utils.DownloadManager;
import cn.wildfire.chat.kit.utils.FileUtils;
import cn.wildfirechat.message.FileMessageContent;

@MessageContentType(FileMessageContent.class)
@EnableContextMenu
public class FileMessageContentViewHolder extends MediaMessageContentViewHolder {
    @BindView(R2.id.fileIconImageView)
    ImageView fileIconImageView;
    @BindView(R2.id.fileNameTextView)
    TextView nameTextView;
    @BindView(R2.id.fileSizeTextView)
    TextView sizeTextView;

    private FileMessageContent fileMessageContent;

    public FileMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public void onBind(UiMessage message) {
        super.onBind(message);
        fileMessageContent = (FileMessageContent) message.message.content;
        nameTextView.setText(fileMessageContent.getName());
        sizeTextView.setText(FileUtils.getReadableFileSize(fileMessageContent.getSize()));
        fileIconImageView.setImageResource(FileUtils.getFileTypeImageResId(fileMessageContent.getName()));
    }

    @OnClick(R2.id.fileMessageContentItemView)
    public void onClick(View view) {
        if (message.isDownloading) {
            return;
        }
        FileUtils.openFile(fragment.getContext(), message.message);
    }

    @MessageContextMenuItem(tag = MessageContextMenuItemTags.TAG_SAVE_FILE, confirm = false, priority = 14)
    public void saveFile(View itemView, UiMessage message) {
        File file = DownloadManager.mediaMessageContentFile(message.message);
        if (file == null || !file.exists()) {
            Toast.makeText(fragment.getContext(), "请先点击下载文件", Toast.LENGTH_SHORT).show();
            return;
        }

        File dstFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + file.getName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + file.getName());
        } else {
            dstFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + file.getName());
        }

        boolean result = FileUtils.copyFile(file, dstFile);
        if (result) {
            Toast.makeText(fragment.getContext(), "文件保存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(fragment.getContext(), "文件保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (MessageContextMenuItemTags.TAG_SAVE_FILE.equals(tag)) {
            return "存储到手机";
        }
        return super.contextMenuTitle(context, tag);
    }

}
