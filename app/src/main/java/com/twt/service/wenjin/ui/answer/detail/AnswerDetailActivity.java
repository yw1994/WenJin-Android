package com.twt.service.wenjin.ui.answer.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.twt.service.wenjin.R;
import com.twt.service.wenjin.api.ApiClient;
import com.twt.service.wenjin.bean.Answer;
import com.twt.service.wenjin.support.FormatHelper;
import com.twt.service.wenjin.support.LogHelper;
import com.twt.service.wenjin.support.UmengShareHelper;
import com.twt.service.wenjin.ui.BaseActivity;
import com.twt.service.wenjin.ui.answer.comment.CommentActivity;
import com.twt.service.wenjin.ui.common.PicassoImageGetter;
import com.twt.service.wenjin.ui.profile.ProfileActivity;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AnswerDetailActivity extends BaseActivity implements AnswerDetailView, View.OnClickListener {

    private static final String LOG_TAG = AnswerDetailActivity.class.getSimpleName();

    private static final String PARAM_ANSWER_ID = "answer_id";
    private static final String PARAM_QUESTION = "question";

    @Inject
    AnswerDetailPresenter mPresenter;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.pb_answer_loading)
    ProgressBar mPbLoading;
    @InjectView(R.id.iv_answer_avatar)
    ImageView ivAvatar;
    @InjectView(R.id.iv_answer_agree)
    ImageView ivAgree;
    @InjectView(R.id.tv_answer_agree_number)
    TextView tvAgreeNumber;
    @InjectView(R.id.tv_answer_username)
    TextView tvUsername;
    @InjectView(R.id.tv_answer_signature)
    TextView tvSignature;
    @InjectView(R.id.tv_answer_content)
    TextView tvContent;
    @InjectView(R.id.tv_answer_add_time)
    TextView tvAddTime;
    private TextView tvCommentCount;

    private int answerId;
    private Answer answer;

    public static void actionStart(Context context, int answerId, String question) {
        Intent intent = new Intent(context, AnswerDetailActivity.class);
        intent.putExtra(PARAM_ANSWER_ID, answerId);
        intent.putExtra(PARAM_QUESTION, question);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_detail);
        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            answerId = savedInstanceState.getInt(PARAM_ANSWER_ID);
        } else {
            answerId = getIntent().getIntExtra(PARAM_ANSWER_ID, 0);
        }
        LogHelper.v(LOG_TAG, "answer id: " + answerId);

        String question = getIntent().getStringExtra(PARAM_QUESTION);
        toolbar.setTitle(question);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPresenter.loadAnswer(answerId);

        ivAgree.setOnClickListener(this);
        ivAvatar.setOnClickListener(this);
        tvUsername.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARAM_ANSWER_ID, answerId);
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AnswerDetailModule(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_answer_detail, menu);
        View menuComment = menu.findItem(R.id.action_comment).getActionView();
        menuComment.setOnClickListener(this);
        tvCommentCount = (TextView) menuComment.findViewById(R.id.tv_action_comment_number);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                UmengShareHelper.init(this);
                UmengShareHelper.setContent(
                        this,
                        getIntent().getStringExtra(PARAM_QUESTION),
                        FormatHelper.formatQuestionLink(answer.question_id)
                );

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_answer_agree:
                mPresenter.actionVote(answerId, 1);
                break;
            case R.id.iv_answer_avatar:
                startProfileActivity();
                break;
            case R.id.tv_answer_username:
                startProfileActivity();
                break;
            case R.id.action_comment:
                if (!tvCommentCount.getText().toString().equals("…")) {
                    startCommentActivity();
                }
                break;
        }
    }

    @Override
    public void showProgressBar() {
        mPbLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mPbLoading.setVisibility(View.GONE);
    }

    @Override
    public void bindAnswerData(Answer answer) {
        this.answer = answer;
        if (!TextUtils.isEmpty(answer.avatar_file)) {
            Picasso.with(this).load(ApiClient.getAvatarUrl(answer.avatar_file)).into(ivAvatar);
        }
        tvUsername.setText(answer.nick_name);
        tvSignature.setText(answer.signature);
        ivAgree.setVisibility(View.VISIBLE);
        if (answer.vote_value == 1) {
            ivAgree.setImageResource(R.drawable.ic_action_agreed);
        } else {
            ivAgree.setImageResource(R.drawable.ic_action_agree);
        }
        tvAgreeNumber.setText("" + answer.agree_count);
        tvContent.setText(Html.fromHtml(answer.answer_content, new PicassoImageGetter(this, tvContent), null));
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvAddTime.setText(FormatHelper.formatAddDate(answer.add_time));
        tvCommentCount.setText("" + answer.comment_count);
    }

    @Override
    public void toastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setAgree(boolean isAgree, int agreeCount) {
        if (isAgree) {
            ivAgree.setImageResource(R.drawable.ic_action_agreed);
        } else {
            ivAgree.setImageResource(R.drawable.ic_action_agree);
        }
        tvAgreeNumber.setText("" + agreeCount);
    }

    @Override
    public void startProfileActivity() {
        ProfileActivity.actionStart(this, answer.uid);
    }

    @Override
    public void startCommentActivity() {
        CommentActivity.actionStart(this, answerId, Integer.parseInt(tvCommentCount.getText().toString()));
    }

}
