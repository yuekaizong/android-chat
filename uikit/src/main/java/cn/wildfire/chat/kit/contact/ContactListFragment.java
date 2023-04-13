/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.Map;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.channel.ChannelListActivity;
import cn.wildfire.chat.kit.contact.model.ContactCountFooterValue;
import cn.wildfire.chat.kit.contact.model.FriendRequestValue;
import cn.wildfire.chat.kit.contact.model.GroupValue;
import cn.wildfire.chat.kit.contact.model.HeaderValue;
import cn.wildfire.chat.kit.contact.model.OrganizationValue;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.contact.newfriend.FriendRequestListActivity;
import cn.wildfire.chat.kit.contact.viewholder.footer.ContactCountViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.ChannelViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.DepartViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.FriendRequestViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.GroupViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.HeaderViewHolder;
import cn.wildfire.chat.kit.contact.viewholder.header.OrganizationViewHolder;
import cn.wildfire.chat.kit.group.GroupListActivity;
import cn.wildfire.chat.kit.organization.OrganizationMemberListActivity;
import cn.wildfire.chat.kit.organization.model.Organization;
import cn.wildfire.chat.kit.user.UserInfoActivity;
import cn.wildfire.chat.kit.widget.QuickIndexBar;
import cn.wildfirechat.model.ChannelInfo;
import cn.wildfirechat.model.UserOnlineState;
import cn.wildfirechat.remote.ChatManager;

public class ContactListFragment extends BaseUserListFragment implements QuickIndexBar.OnLetterUpdateListener {
    private boolean pick = false;
    private boolean showChannel = true;
    private List<String> filterUserList;
    private static final int REQUEST_CODE_PICK_CHANNEL = 100;

    private OrganizationServiceViewModel organizationServiceViewModel;

    private boolean isVisibleToUser = false;
    private ContactViewModel contactViewModel;


    @Override
    public void setMenuVisibility(boolean isvisible) {
        super.setMenuVisibility(isvisible);
        this.isVisibleToUser = isvisible;
        if (isvisible) {
            contactViewModel.reloadContact();
            contactViewModel.reloadFriendRequestStatus();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            pick = bundle.getBoolean("pick", false);
            showChannel = bundle.getBoolean("showChannel", true);
            filterUserList = bundle.getStringArrayList("filterUserList");
        }
        organizationServiceViewModel = new ViewModelProvider(this).get(OrganizationServiceViewModel.class);
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisibleToUser) {
            contactViewModel.reloadContact();
            contactViewModel.reloadFriendRequestStatus();
            contactViewModel.reloadFavContact();
        }
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        contactViewModel.contactListLiveData().observe(this, userInfos -> {
            showContent();
            if (filterUserList != null) {
                userInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            patchUserOnlineState(userInfos);
            userListAdapter.setUsers(userInfos);
        });
        contactViewModel.friendRequestUpdatedLiveData().observe(getActivity(), integer -> userListAdapter.updateHeader(0, new FriendRequestValue(integer)));
        contactViewModel.favContactListLiveData().observe(getActivity(), uiUserInfos -> {
            if (filterUserList != null) {
                uiUserInfos.removeIf(uiUserInfo -> filterUserList.indexOf(uiUserInfo.getUserInfo().uid) > -1);
            }
            patchUserOnlineState(uiUserInfos);
            userListAdapter.setFavUsers(uiUserInfos);
        });

    }

    private void patchUserOnlineState(List<UIUserInfo> userInfos) {
        if (userInfos == null) {
            return;
        }
        Map<String, UserOnlineState> userOnlineStateMap = ChatManager.Instance().getUserOnlineStateMap();
        for (UIUserInfo userInfo : userInfos) {
            UserOnlineState userOnlineState = userOnlineStateMap.get(userInfo.getUserInfo().uid);
            if (userOnlineState != null) {
                String userOnlineDesc = userOnlineState.desc();
                if (!TextUtils.isEmpty(userOnlineDesc)) {
                    userInfo.setDesc(userOnlineDesc);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void initHeaderViewHolders() {
        if (!pick) {
            addHeaderViewHolder(FriendRequestViewHolder.class, R.layout.contact_header_friend, new FriendRequestValue(contactViewModel.getUnreadFriendRequestCount()));
            addHeaderViewHolder(GroupViewHolder.class, R.layout.contact_header_group, new GroupValue());
        }
        if (showChannel) {
            addHeaderViewHolder(ChannelViewHolder.class, R.layout.contact_header_channel, new HeaderValue());
        }
        if (!TextUtils.isEmpty(Config.ORG_SERVER_ADDRESS)) {
            organizationServiceViewModel.rootOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
                @Override
                public void onChanged(List<Organization> organizations) {
                    if (!organizations.isEmpty()) {
                        for (Organization org : organizations) {
                            OrganizationValue value = new OrganizationValue();
                            value.setValue(org);
                            addHeaderViewHolder(OrganizationViewHolder.class, R.layout.contact_header_organization, value);
                        }
                    }
                }
            });
            organizationServiceViewModel.myOrganizationLiveData().observe(this, new Observer<List<Organization>>() {
                @Override
                public void onChanged(List<Organization> organizations) {
                    if (!organizations.isEmpty()) {
                        for (Organization org : organizations) {
                            OrganizationValue value = new OrganizationValue();
                            value.setValue(org);
                            addHeaderViewHolder(DepartViewHolder.class, R.layout.contact_header_department, value);
                        }
                    }

                }
            });
        }
    }

    @Override
    public void initFooterViewHolders() {
        addFooterViewHolder(ContactCountViewHolder.class, R.layout.contact_item_footer, new ContactCountFooterValue());
    }

    @Override
    public void onUserClick(UIUserInfo userInfo) {
        if (pick) {
            Intent intent = new Intent();
            intent.putExtra("userInfo", userInfo.getUserInfo());
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            Intent intent = new Intent(getActivity(), UserInfoActivity.class);
            intent.putExtra("userInfo", userInfo.getUserInfo());
            startActivity(intent);
        }
    }

    @Override
    public void onHeaderClick(HeaderViewHolder holder) {
        if (pick) {
            showChannelList();
            return;
        }
        if (holder instanceof FriendRequestViewHolder) {
            showFriendRequest();
        } else if (holder instanceof GroupViewHolder) {
            showGroupList();
        } else if (holder instanceof ChannelViewHolder) {
            showChannelList();
        } else if (holder instanceof OrganizationViewHolder) {
            showOrganizationMemberList(((OrganizationViewHolder) holder).getOrganization());
        } else if (holder instanceof DepartViewHolder) {
            showOrganizationMemberList(((DepartViewHolder) holder).getOrganization());
        }
    }

    private void showFriendRequest() {
        FriendRequestValue value = new FriendRequestValue(0);
        userListAdapter.updateHeader(0, value);

        Intent intent = new Intent(getActivity(), FriendRequestListActivity.class);
        startActivity(intent);
    }

    private void showGroupList() {
        Intent intent = new Intent(getActivity(), GroupListActivity.class);
        startActivity(intent);
    }

    private void showChannelList() {
        Intent intent = new Intent(getActivity(), ChannelListActivity.class);
        if (pick) {
            intent.putExtra("pick", true);
            startActivityForResult(intent, REQUEST_CODE_PICK_CHANNEL);
        } else {
            startActivity(intent);
        }
    }

    private void showOrganizationMemberList(Organization org) {
        Intent intent = new Intent(getActivity(), OrganizationMemberListActivity.class);
        intent.putExtra("organizationId", org.id);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_PICK_CHANNEL && resultCode == Activity.RESULT_OK) {
            Intent intent = new Intent();
            ChannelInfo channelInfo = data.getParcelableExtra("channelInfo");
            intent.putExtra("channelInfo", channelInfo);
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
