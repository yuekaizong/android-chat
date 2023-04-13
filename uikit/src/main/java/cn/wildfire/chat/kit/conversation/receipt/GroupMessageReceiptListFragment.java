/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.receipt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.group.GroupViewModel;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.user.UserViewModel;
import cn.wildfire.chat.kit.widget.ProgressFragment;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class GroupMessageReceiptListFragment extends ProgressFragment implements GroupMessageReceiptAdapter.OnMemberClickListener {
    private GroupInfo groupInfo;
    private GroupMessageReceiptAdapter groupMemberListAdapter;

    private boolean unread;
    private Message message;

    @BindView(R2.id.recyclerView)
    RecyclerView recyclerView;

    public static GroupMessageReceiptListFragment newInstance(GroupInfo groupInfo, Message message, boolean unread) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        args.putParcelable("message", message);
        args.putBoolean("unread", unread);
        GroupMessageReceiptListFragment fragment = new GroupMessageReceiptListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
        this.message = getArguments().getParcelable("message");
        this.unread = getArguments().getBoolean("unread");
    }

    @Override
    protected int contentLayout() {
        return R.layout.conversation_receipt_fragment;
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        ButterKnife.bind(this, view);
        groupMemberListAdapter = new GroupMessageReceiptAdapter(groupInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(groupMemberListAdapter);
        groupMemberListAdapter.setOnMemberClickListener(this);
        UserViewModel userViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        userViewModel.userInfoLiveData().observe(this, userInfos -> loadAndShowGroupMembers());
        loadAndShowGroupMembers();
    }

    private void loadAndShowGroupMembers() {
        GroupViewModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);
        groupViewModel.getGroupMemberUserInfosLiveData(groupInfo.target, false).observe(this, userInfos -> {
            showContent();
            groupMemberListAdapter.setMembers(filterGroupMember(userInfos, unread));
            groupMemberListAdapter.notifyDataSetChanged();
        });
    }

    private List<UserInfo> filterGroupMember(List<UserInfo> userInfos, boolean unread) {
        Map<String, Long> readEntries = ChatManager.Instance().getConversationRead(this.message.conversation);
        List<UserInfo> result = new ArrayList<>();
        for (UserInfo info : userInfos) {
            Long readDt = readEntries.get(info.uid);
            if (unread) {
                if (readDt == null || readDt < message.serverTime) {
                    result.add(info);
                }
            } else {
                if (readDt != null && readDt >= message.serverTime) {
                    result.add(info);
                }
            }
        }
        return result;
    }

    @Override
    public void onUserMemberClick(UserInfo userInfo) {
        GroupMember groupMember = ChatManager.Instance().getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
        if (groupInfo != null && groupInfo.privateChat == 1 && groupMember.type == GroupMember.GroupMemberType.Normal) {
            return;
        }
        Intent intent = new Intent(getActivity(), UserInfoActivity.class);
        intent.putExtra("userInfo", userInfo);
        intent.putExtra("groupId", groupInfo.target);
        startActivity(intent);
    }
}
