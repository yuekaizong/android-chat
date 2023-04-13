/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.common.OperateResult;
import cn.wildfirechat.model.GroupInfo;

public class SetGroupRemarkActivity extends WfcBaseActivity {
    @BindView(R2.id.remarkEditText)
    EditText remarkEditText;

    private MenuItem confirmMenuItem;
    private GroupInfo groupInfo;
    private GroupViewModel groupViewModel;

    @Override
    protected int contentLayout() {
        return R.layout.group_set_remark_activity;
    }

    @Override
    protected void afterViews() {
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        if (groupInfo == null) {
            finish();
            return;
        }
        groupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);

        if (!TextUtils.isEmpty(groupInfo.remark)) {
            remarkEditText.setText(groupInfo.remark);
            remarkEditText.setSelection(groupInfo.remark.length());
        }
    }

    @Override
    protected int menu() {
        return R.menu.group_set_group_remark;
    }

    @Override
    protected void afterMenus(Menu menu) {
        confirmMenuItem = menu.findItem(R.id.confirm);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.confirm) {
            setGroupRemark();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGroupRemark() {
        String remark = remarkEditText.getText().toString().trim();
        if (remark.equals(groupInfo.remark)) {
            finish();
            return;
        }
        groupInfo.remark = remark;
        MaterialDialog dialog = new MaterialDialog.Builder(this)
            .content("请稍后...")
            .progress(true, 100)
            .cancelable(false)
            .build();
        dialog.show();

        groupViewModel.setGroupRemark(groupInfo.target, groupInfo.remark).observe(this, new Observer<OperateResult<Boolean>>() {
            @Override
            public void onChanged(@Nullable OperateResult operateResult) {
                dialog.dismiss();
                if (operateResult.isSuccess()) {
                    Toast.makeText(SetGroupRemarkActivity.this, "修改群备注成功", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SetGroupRemarkActivity.this, "修改群备注失败: " + operateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
