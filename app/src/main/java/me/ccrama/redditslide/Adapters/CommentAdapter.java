package me.ccrama.redditslide.Adapters;

/**
 * Created by ccrama on 3/22/2015.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.cocosw.bottomsheet.BottomSheet;
import com.devspark.robototextview.util.RobotoTypefaceManager;

import net.dean.jraw.ApiException;
import net.dean.jraw.managers.AccountManager;
import net.dean.jraw.managers.ModerationManager;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.CommentNode;
import net.dean.jraw.models.Contribution;
import net.dean.jraw.models.DistinguishedStatus;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.VoteDirection;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator;
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator;
import me.ccrama.redditslide.ActionStates;
import me.ccrama.redditslide.Activities.Profile;
import me.ccrama.redditslide.Activities.Website;
import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Fragments.CommentPage;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.SpoilerRobotoTextView;
import me.ccrama.redditslide.SubmissionViews.PopulateSubmissionViewHolder;
import me.ccrama.redditslide.SubredditStorage;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Views.DoEditorActions;
import me.ccrama.redditslide.Views.PreCachingLayoutManagerComments;
import me.ccrama.redditslide.Views.RoundedBackgroundSpan;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;
import me.ccrama.redditslide.Vote;
import me.ccrama.redditslide.util.LogUtil;
import me.ccrama.redditslide.util.SubmissionParser;


public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final static int HEADER = 1;
    private final int SPACER = 6;
    public Context mContext;
    public SubmissionComments dataSet;
    public Submission submission;
    public CommentViewHolder currentlySelected;
    public String currentSelectedItem = "";
    public int shiftFrom;
    public FragmentManager fm;
    public int clickpos;
    public int currentPos;
    public CommentViewHolder isHolder;
    public boolean isClicking;
    public HashMap<String, Integer> keys = new HashMap<>();
    public ArrayList<CommentObject> users;
    public ArrayList<String> deleted = new ArrayList<>();
    RecyclerView listView;
    ArrayList<String> up;
    ArrayList<String> down;
    CommentPage mPage;
    boolean isSame;
    int shifted;
    int toShiftTo;
    ArrayList<String> hidden;
    ArrayList<String> hiddenPersons;
    ArrayList<String> replie;

    public CommentAdapter(CommentPage mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, FragmentManager fm) {
        this.mContext = mContext.getContext();
        mPage = mContext;
        this.listView = listView;
        this.dataSet = dataSet;
        this.fm = fm;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();
        up = new ArrayList<>();
        down = new ArrayList<>();

        shifted = 0;

        isSame = false;

    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission, int oldSize) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        isSame = false;
        notifyDataSetChanged();
        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 1;

            for (CommentObject n : users) {

                if (n.getName().contains(currentSelectedItem) && !(n instanceof MoreChildItem)) {
                    CommentPage.TopSnappedSmoothScroller scroller = new CommentPage.TopSnappedSmoothScroller(mContext, (PreCachingLayoutManagerComments) listView.getLayoutManager());
                    scroller.setTargetPosition(i);
                    (listView.getLayoutManager()).startSmoothScroll(scroller);
                    break;
                }
                i++;
            }
        }
    }

    public void reset(Context mContext, SubmissionComments dataSet, RecyclerView listView, Submission submission) {

        this.mContext = mContext;
        this.listView = listView;
        this.dataSet = dataSet;

        this.submission = submission;
        hidden = new ArrayList<>();
        users = dataSet.comments;
        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                keys.put(users.get(i).getName(), i);
            }
        }
        hiddenPersons = new ArrayList<>();
        replie = new ArrayList<>();


        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            notifyDataSetChanged();
        } else {
            if (users != null) {
                notifyItemRangeChanged(2, users.size() + 1);
            } else {
                users = new ArrayList<>();
                notifyDataSetChanged();
            }
        }
        isSame = false;


        if (currentSelectedItem != null && !currentSelectedItem.isEmpty()) {
            int i = 2;
            for (CommentObject n : users) {
                if (n instanceof CommentItem && n.comment.getComment().getFullName().contains(currentSelectedItem)) {
                    ((PreCachingLayoutManagerComments) listView.getLayoutManager()).scrollToPositionWithOffset(i, mPage.getActivity().findViewById(R.id.header).getHeight());
                    break;
                }
                i++;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        switch (i) {
            case SPACER: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.spacer_post, viewGroup, false);
                RecyclerView.ViewHolder v2 = new SpacerViewHolder(v);
                return v2;
            }
            case HEADER: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.submission_fullscreen, viewGroup, false);
                RecyclerView.ViewHolder v2 = new SubmissionViewHolder(v);
                return v2;
            }
            case 2: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment, viewGroup, false);
                RecyclerView.ViewHolder v2 = new CommentViewHolder(v);
                return v2;
            }
            default: {
                View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.morecomment, viewGroup, false);
                RecyclerView.ViewHolder v2 = new MoreCommentViewHolder(v);
                return v2;
            }
        }


    }

    public class SpacerViewHolder extends RecyclerView.ViewHolder {
        public SpacerViewHolder(View itemView) {
            super(itemView);
        }
    }


    public void setError(boolean b) {
        listView.setAdapter(new ErrorAdapter());
    }


    public void doScoreText(CommentViewHolder holder, Comment comment, int offset) {
        String spacer = " " + mContext.getString(R.string.submission_properties_seperator_comments) + " ";
        SpannableStringBuilder titleString = new SpannableStringBuilder();

        String distingush = "";
        if (comment.getDistinguishedStatus() == DistinguishedStatus.MODERATOR) {
            distingush = "[M]";
        } else if (comment.getDistinguishedStatus() == DistinguishedStatus.ADMIN) {
            distingush = "[A]";
        }

        SpannableStringBuilder author = new SpannableStringBuilder(" " + distingush + comment.getAuthor() + " ");
        int authorcolor = Palette.getFontColorUser(comment.getAuthor());

        author.setSpan(new TypefaceSpan("sans-serif-condensed"), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        author.setSpan(new StyleSpan(Typeface.BOLD), 0, author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (comment.getAuthor().toLowerCase().equals(Authentication.name.toLowerCase())) {
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_deep_orange_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (!distingush.isEmpty()) {
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (comment.getAuthor().toLowerCase().equals(submission.getAuthor().toLowerCase())) {
            author.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_blue_300, false), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else if (authorcolor != 0) {
            author.setSpan(new ForegroundColorSpan(authorcolor), 0, author.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        titleString.append(author);

        titleString.append(spacer);

        String scoreText;
        if (comment.isScoreHidden()) {
            scoreText = "[" + mContext.getString(R.string.misc_score_hidden).toUpperCase() + "]";
        } else {
            scoreText = Integer.toString(comment.getScore() + offset);
        }
        SpannableStringBuilder score = new SpannableStringBuilder(scoreText);
        int scoreColor;

        if (up.contains(comment.getFullName())) {
            scoreColor = (holder.textColorUp);
        } else if (down.contains(comment.getFullName())) {
            scoreColor = (holder.textColorDown);
        } else {
            scoreColor = (holder.textColorRegular);
        }

        score.setSpan(new ForegroundColorSpan(scoreColor), 0, score.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        titleString.append(score);
        titleString.append((comment.isControversial() ? "†" : ""));

        titleString.append(spacer);
        titleString.append(TimeUtils.getTimeAgo(comment.getCreated().getTime(), mContext));

        titleString.append(((comment.hasBeenEdited() && comment.getEditDate() != null) ? " *" + TimeUtils.getTimeAgo(comment.getEditDate().getTime(), mContext) : ""));
        titleString.append("  ");

        if (comment.getDataNode().get("stickied").asBoolean()) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + mContext.getString(R.string.sidebar_pinned).toUpperCase() + " ");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_green_300, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getTimesGilded() > 0) {
            SpannableStringBuilder pinned = new SpannableStringBuilder(" ★" + comment.getTimesGilded() + " ");
            pinned.setSpan(new RoundedBackgroundSpan(mContext, R.color.white, R.color.md_orange_500, false), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        if (comment.getAuthorFlair() != null && comment.getAuthorFlair().getText() != null && !comment.getAuthorFlair().getText().isEmpty()) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.activity_background, typedValue, true);
            int color = typedValue.data;
            SpannableStringBuilder pinned = new SpannableStringBuilder(" " + comment.getAuthorFlair().getText() + " ");
            pinned.setSpan(new RoundedBackgroundSpan(holder.firstTextView.getCurrentTextColor(), color, false, mContext), 0, pinned.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            titleString.append(pinned);
            titleString.append(" ");
        }
        holder.content.setText(titleString);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder firstHolder, int old) {
        int pos = old != 0 ? old - 1 : old;

        if (firstHolder instanceof CommentViewHolder) {
            final CommentViewHolder holder = (CommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);
            final int finalPos = nextPos;
            final int finalPos1 = pos;


            if (pos > toShiftTo) {
                shifted = 0;
            }
            if (pos < shiftFrom) {
                shifted = 0;
            }
            final CommentNode baseNode = users.get(nextPos).comment;
            final Comment comment = baseNode.getComment();


            if (pos == getItemCount() - 1) {
                holder.itemView.setPadding(0, 0, 0, (int) mContext.getResources().getDimension(R.dimen.overview_top_padding_single));
            } else {
                holder.itemView.setPadding(0, 0, 0, 0);
            }

            if (comment.getVote() == VoteDirection.UPVOTE) {
                if (!up.contains(comment.getFullName())) {
                    up.add(comment.getFullName());
                }
            } else if (comment.getVote() == VoteDirection.DOWNVOTE) {
                if (!down.contains(comment.getFullName())) {
                    down.add(comment.getFullName());
                }
            }

            doScoreText(holder, comment, 0);


            holder.firstTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (SettingValues.swap) {
                        doOnClick(holder, comment, baseNode);
                    } else {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    }
                    return true;
                }
            });
            Typeface typeface = RobotoTypefaceManager.obtainTypeface(
                    mContext,
                    new FontPreferences(mContext).getFontTypeComment().getTypeface());
            holder.firstTextView.setTypeface(typeface);
            setViews(comment.getDataNode().get("body_html").asText(), submission.getSubredditName(), holder);

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (SettingValues.swap) {
                        doOnClick(holder, comment, baseNode);
                    } else {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    }
                    return true;
                }
            });

            if (baseNode.isTopLevel()) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.VISIBLE);
            } else if (holder.itemView.findViewById(R.id.next).getVisibility() == View.VISIBLE) {
                holder.itemView.findViewById(R.id.next).setVisibility(View.GONE);
            }


            if (hiddenPersons.contains(comment.getFullName())) {
                holder.children.setVisibility(View.VISIBLE);
                holder.childrenNumber.setText("+" + getChildNumber(baseNode));
                if (SettingValues.collapseComments) {
                    holder.firstTextView.setVisibility(View.GONE);
                    holder.commentOverflow.setVisibility(View.GONE);
                }
            } else {
                holder.children.setVisibility(View.GONE);
                holder.firstTextView.setVisibility(View.VISIBLE);
                holder.commentOverflow.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SettingValues.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else {
                        doOnClick(holder, comment, baseNode);
                    }
                }
            });
            holder.firstTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SpoilerRobotoTextView SpoilerRobotoTextView = (SpoilerRobotoTextView) v;
                    if (SettingValues.swap) {
                        doLongClick(holder, comment, baseNode, finalPos, finalPos1);
                    } else if (!SpoilerRobotoTextView.isSpoilerClicked()) {
                        doOnClick(holder, comment, baseNode);
                    } else if (SpoilerRobotoTextView.isSpoilerClicked()) {
                        SpoilerRobotoTextView.resetSpoilerClicked();
                    }
                }
            });


            holder.dot.setVisibility(View.VISIBLE);

            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;

            //Padding on the left, starting with the third comment
            for (int i = 2; i < baseNode.getDepth(); i++) {
                width += dwidth;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(width, 0, 0, 0);
            holder.itemView.setLayoutParams(params);

            if (baseNode.getDepth() - 1 > 0) {
                int i22 = baseNode.getDepth() - 2;
                if (i22 % 5 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_700 : R.color.md_blue_500));
                } else if (i22 % 4 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_600 : R.color.md_green_500));
                } else if (i22 % 3 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_500 : R.color.md_yellow_500));
                } else if (i22 % 2 == 0) {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_400 : R.color.md_orange_500));
                } else {
                    holder.dot.setBackgroundColor(ContextCompat.getColor(mContext, !SettingValues.colorCommentDepth ? R.color.md_grey_300 : R.color.md_red_500));
                }
            } else {
                holder.dot.setVisibility(View.GONE);
            }
            if (comment.getFullName().contains(currentSelectedItem) && !currentSelectedItem.isEmpty()) {
                doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
            } else {
                doUnHighlighted(holder, baseNode);
            }
            if (deleted.contains(comment.getFullName())) {
                holder.firstTextView.setText(R.string.comment_deleted);
                holder.content.setText(R.string.comment_deleted);
            }
        } else if (firstHolder instanceof SubmissionViewHolder) {
            new PopulateSubmissionViewHolder().populateSubmissionViewHolder((SubmissionViewHolder) firstHolder, submission, (Activity) mContext, true, true, null, null, false, false, null);
            if (Authentication.isLoggedIn && Authentication.didOnline) {
                if (submission.isArchived())
                    firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);
                else {
                    firstHolder.itemView.findViewById(R.id.reply).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final View replyArea = firstHolder.itemView.findViewById(R.id.innerSend);
                            if (replyArea.getVisibility() == View.GONE) {
                                expand(replyArea, true, true);
                                DoEditorActions.doActions(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)), firstHolder.itemView, fm);

                                currentlyEditing = ((EditText) firstHolder.itemView.findViewById(R.id.replyLine));
                                editingPosition = firstHolder.getAdapterPosition();

                                firstHolder.itemView.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dataSet.refreshLayout.setRefreshing(true);
                                        currentlyEditing = null;
                                        editingPosition = -1;
                                        new ReplyTaskComment(submission).execute(((EditText) firstHolder.itemView.findViewById(R.id.replyLine)).getText().toString());
                                        replyArea.setVisibility(View.GONE);

                                        //Hide soft keyboard
                                        View view = ((Activity) mContext).getCurrentFocus();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                    }
                                });
                            } else {
                                collapse(replyArea, true);
                            }
                        }
                    });
                    firstHolder.itemView.findViewById(R.id.discard).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                            currentlyEditing = null;
                            editingPosition = -1;
                        }
                    });
                }
            } else {
                firstHolder.itemView.findViewById(R.id.innerSend).setVisibility(View.GONE);
                firstHolder.itemView.findViewById(R.id.reply).setVisibility(View.GONE);

            }

            firstHolder.itemView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firstHolder.itemView.findViewById(R.id.menu).callOnClick();

                }
            });
        } else if (firstHolder instanceof MoreCommentViewHolder) {
            final MoreCommentViewHolder holder = (MoreCommentViewHolder) firstHolder;
            int nextPos = pos - 1;

            nextPos = getRealPosition(nextPos);

            final MoreChildItem baseNode = (MoreChildItem) users.get(nextPos);
            if (baseNode.children.getCount() > 0) {
                holder.content.setText(mContext.getString(R.string.comment_load_more, baseNode.children.getCount()));
            } else {
                holder.content.setText(R.string.comment_load_more_number_unknown);
            }

            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;
            for (int i = 1; i < baseNode.comment.getDepth(); i++) {
                width += dwidth;
            }

            final View progress = holder.loading;
            progress.setVisibility(View.GONE);
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (progress.getVisibility() == View.GONE) {
                        progress.setVisibility(View.VISIBLE);
                        holder.content.setText(R.string.comment_loading_more);
                        new AsyncLoadMore(getRealPosition(holder.getAdapterPosition() - 2), holder.getAdapterPosition(), holder).execute(baseNode);
                    }
                }
            });

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(width, 0, 0, 0);
            holder.itemView.setLayoutParams(params);
        }
        if (firstHolder instanceof SpacerViewHolder) {
            firstHolder.itemView.findViewById(R.id.height).setLayoutParams(new LinearLayout.LayoutParams(firstHolder.itemView.getWidth(), mPage.getActivity().findViewById(R.id.header).getHeight()));
        }
    }

    private void setViews(String rawHTML, String subredditName, CommentViewHolder holder) {
        if (rawHTML.isEmpty()) {
            return;
        }

        List<String> blocks = SubmissionParser.getBlocks(rawHTML);

        int startIndex = 0;
        // the <div class="md"> case is when the body contains a table or code block first
        if (!blocks.get(0).equals("<div class=\"md\">")) {
            holder.firstTextView.setVisibility(View.VISIBLE);
            holder.firstTextView.setTextHtml(blocks.get(0), subredditName);
            startIndex = 1;
        } else {
            holder.firstTextView.setText("");
            holder.firstTextView.setVisibility(View.GONE);
        }

        if (blocks.size() > 1) {
            if (startIndex == 0) {
                holder.commentOverflow.setViews(blocks, subredditName);
            } else {
                holder.commentOverflow.setViews(blocks.subList(startIndex, blocks.size()), subredditName);
            }
        } else {
            holder.commentOverflow.removeAllViews();
        }
    }

    int editingPosition;

    private ValueAnimator slideAnimator(int start, int end, final View v) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setInterpolator(new FastOutSlowInInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = value;
                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    private void collapse(final View v, boolean full) {
        int finalHeight = v.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void collapse(final View v) {
        int finalHeight = v.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0, v);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                ((LinearLayout) v).removeAllViews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void doShowMenu(final View l) {
        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);


        final View l2 = l.findViewById(R.id.menu);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);
        ValueAnimator mAnimator = slideAnimator(l.getMeasuredHeight(), l2.getMeasuredHeight(), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                l2.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void expand(final View l) {

        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        View l2 = l.findViewById(R.id.replyArea) == null ? l.findViewById(R.id.innerSend) : l.findViewById(R.id.replyArea);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);


        ValueAnimator mAnimator = slideAnimator(0, l.getMeasuredHeight() - l2.getMeasuredHeight(), l);

        mAnimator.start();
    }

    private void expand(final View l, boolean b) {

        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        View l2 = l.findViewById(R.id.replyArea) == null ? l.findViewById(R.id.innerSend) : l.findViewById(R.id.replyArea);
        final int widthSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec2 = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l2.measure(widthSpec2, heightSpec2);


        ValueAnimator mAnimator = slideAnimator((l.getMeasuredHeight() - l2.getMeasuredHeight()), l.getMeasuredHeight() - (l.getMeasuredHeight() - l2.getMeasuredHeight()), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                params.addRule(RelativeLayout.BELOW, R.id.commentOverflow);
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    private void expand(final View l, boolean b, boolean full) {

        l.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        l.measure(widthSpec, heightSpec);

        ValueAnimator mAnimator = slideAnimator(0, l.getMeasuredHeight(), l);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) l.getLayoutParams();
                params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                l.setLayoutParams(params);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mAnimator.start();
    }

    CommentNode currentBaseNode;

    public void doHighlighted(final CommentViewHolder holder, final Comment n, final CommentNode baseNode, final int finalPos, final int finalPos1) {
        if (currentlySelected != null) {
            doUnHighlighted(currentlySelected, currentBaseNode);
        }
        // If a comment is hidden and (Swap long press == true), then a single click will un-hide the comment
        // and expand to show all children comments
        if (SettingValues.swap && holder.firstTextView.getVisibility() == View.GONE) {
            unhideAll(baseNode, holder.getAdapterPosition() + 1);
            hiddenPersons.remove(n.getFullName());
            hideChildrenObject(holder.children);
            holder.firstTextView.setVisibility(View.VISIBLE);
            holder.menuArea.setVisibility(View.GONE);
        } else {
            currentlySelected = holder;
            currentBaseNode = baseNode;
            int color = Palette.getColor(n.getSubredditName());
            currentSelectedItem = n.getFullName();

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            final View baseView = inflater.inflate(R.layout.comment_menu, holder.menuArea);
            baseView.setVisibility(View.GONE);
            expand(baseView);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            holder.itemView.setLayoutParams(params);

            View reply = baseView.findViewById(R.id.reply);
            View send = baseView.findViewById(R.id.send);

            final View menu = baseView.findViewById(R.id.menu);
            final View replyArea = baseView.findViewById(R.id.replyArea);

            final View more = baseView.findViewById(R.id.more);
            final ImageView upvote = (ImageView) baseView.findViewById(R.id.upvote);
            final ImageView downvote = (ImageView) baseView.findViewById(R.id.downvote);
            View discard = baseView.findViewById(R.id.discard);
            final EditText replyLine = (EditText) baseView.findViewById(R.id.replyLine);


            if (up.contains(n.getFullName())) {
                upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
            } else if (down.contains(n.getFullName())) {
                downvote.setColorFilter(holder.textColorDown, PorterDuff.Mode.MULTIPLY);
            } else {
                downvote.clearColorFilter();
                upvote.clearColorFilter();
            }
            {
                final ImageView mod = (ImageView) baseView.findViewById(R.id.mod);
                try {
                    if (SubredditStorage.modOf.contains(submission.getSubredditName())) {
                        //todo
                        mod.setVisibility(View.GONE);

                    } else {
                        mod.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.d(LogUtil.getTag(), "Error loading mod " + e.toString());
                }
            }
            {
                final ImageView edit = (ImageView) baseView.findViewById(R.id.edit);
                if (Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase())) {
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

                            final View dialoglayout = inflater.inflate(R.layout.edit_comment, null);
                            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(mContext);

                            final EditText e = (EditText) dialoglayout.findViewById(R.id.entry);
                            e.setText(StringEscapeUtils.unescapeHtml4(baseNode.getComment().getBody()));


                            builder.setView(dialoglayout);
                            final Dialog d = builder.create();
                            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                            d.show();
                            dialoglayout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    d.dismiss();
                                }
                            });
                            dialoglayout.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            try {
                                                new AccountManager(Authentication.reddit).updateContribution(baseNode.getComment(), e.getText().toString());
                                                dataSet.loadMore(CommentAdapter.this, submission.getSubredditName());


                                                currentSelectedItem = baseNode.getComment().getFullName();
                                                d.dismiss();
                                            } catch (Exception e) {
                                                ((Activity) mContext).runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        new AlertDialogWrapper.Builder(mContext)
                                                                .setTitle(R.string.comment_delete_err)
                                                                .setMessage(R.string.comment_delete_err_msg)
                                                                .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                        doInBackground();
                                                                    }
                                                                }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                    }
                                                });
                                            }
                                            return null;
                                        }
                                    }.execute();
                                }
                            });


                        }
                    });
                } else {
                    edit.setVisibility(View.GONE);
                }
            }
            {
                final ImageView delete = (ImageView) baseView.findViewById(R.id.delete);
                if (Authentication.name.toLowerCase().equals(baseNode.getComment().getAuthor().toLowerCase())) {
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            new AlertDialogWrapper.Builder(mContext)
                                    .setTitle(R.string.comment_delete)
                                    .setMessage(R.string.comment_delete_msg)
                                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new AsyncTask<Void, Void, Void>() {

                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    try {
                                                        new ModerationManager(Authentication.reddit).delete(baseNode.getComment());
                                                        deleted.add(baseNode.getComment().getFullName());

                                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                holder.firstTextView.setTextHtml("[deleted]");
                                                                holder.content.setText("[deleted]");
                                                            }
                                                        });

                                                    } catch (ApiException e) {
                                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                new AlertDialogWrapper.Builder(mContext)
                                                                        .setTitle(R.string.comment_delete_err)
                                                                        .setMessage(R.string.comment_delete_err_msg)
                                                                        .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(DialogInterface dialog, int which) {
                                                                                dialog.dismiss();
                                                                                doInBackground();
                                                                            }
                                                                        }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        dialog.dismiss();
                                                                    }
                                                                }).show();
                                                            }
                                                        });

                                                        e.printStackTrace();
                                                    }

                                                    return null;
                                                }
                                            }.execute();

                                        }
                                    })
                                    .setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();

                            //todo delete
                        }
                    });
                } else {
                    delete.setVisibility(View.GONE);
                }
            }
            if (Authentication.isLoggedIn && !submission.isArchived() && Authentication.didOnline) {
                reply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expand(baseView, true);
                        replyArea.setVisibility(View.VISIBLE);
                        menu.setVisibility(View.GONE);
                        DoEditorActions.doActions(replyLine, replyArea, fm);
                        currentlyEditing = replyLine;
                        editingPosition = holder.getAdapterPosition();

                    }
                });
                send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentlyEditing = null;
                        editingPosition = -1;

                        doShowMenu(baseView);

                        dataSet.refreshLayout.setRefreshing(true);
                        new ReplyTaskComment(n, finalPos, finalPos1, baseNode).execute(replyLine.getText().toString());

                        //Hide soft keyboard
                        View view = ((Activity) mContext).getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }

                    }
                });
                discard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doShowMenu(baseView);
                    }
                });

            } else {

                if (reply.getVisibility() == View.VISIBLE)

                    reply.setVisibility(View.GONE);
                if (upvote.getVisibility() == View.VISIBLE)

                    upvote.setVisibility(View.GONE);
                if (downvote.getVisibility() == View.VISIBLE)

                    downvote.setVisibility(View.GONE);

            }

            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBottomSheet(mContext, holder, n);
                }
            });
            upvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doUnHighlighted(holder, baseNode.getComment(), baseNode);
                    if (up.contains(n.getFullName())) {
                        new Vote(v, mContext).execute(n);
                        up.remove(n.getFullName());
                        doScoreText(holder, n, 0);
                        upvote.clearColorFilter();

                    } else if (down.contains(n.getFullName())) {
                        new Vote(true, v, mContext).execute(n);
                        up.add(n.getFullName());
                        down.remove(n.getFullName());
                        downvote.clearColorFilter(); // reset colour
                        doScoreText(holder, n, 1);
                        upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                    } else {
                        new Vote(true, v, mContext).execute(n);
                        up.add(n.getFullName());
                        doScoreText(holder, n, 1);
                        upvote.setColorFilter(holder.textColorUp, PorterDuff.Mode.MULTIPLY);
                    }
                }
            });
            downvote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doUnHighlighted(holder, baseNode.getComment(), baseNode);

                    if (down.contains(n.getFullName())) {
                        new Vote(v, mContext).execute(n);
                        down.remove(n.getFullName());
                        doScoreText(holder, n, 0);
                        downvote.clearColorFilter();

                    } else if (up.contains(n.getFullName())) {
                        new Vote(false, v, mContext).execute(n);
                        down.add(n.getFullName());
                        up.remove(n.getFullName());

                        upvote.clearColorFilter(); // reset colour
                        doScoreText(holder, n, -1);
                        downvote.setColorFilter(holder.textColorDown);

                    } else {
                        new Vote(false, v, mContext).execute(n);
                        down.add(n.getFullName());
                        doScoreText(holder, n, -1);
                        downvote.setColorFilter(holder.textColorDown);
                    }
                }
            });
            menu.setBackgroundColor(color);
            replyArea.setBackgroundColor(color);

            menu.setVisibility(View.VISIBLE);
            replyArea.setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.background).setBackgroundColor(Color.argb(50, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }

    public EditText currentlyEditing;

    public void doUnHighlighted(final CommentViewHolder holder, final CommentNode baseNode) {
        collapse(holder.menuArea);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = mContext.getTheme();
        theme.resolveAttribute(R.attr.card_background, typedValue, true);
        int color = typedValue.data;
        int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
        int width = 0;

        //Padding on the left, starting with the third comment
        for (int i = 2; i < baseNode.getDepth(); i++) {
            width += dwidth;
        }
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        params.setMargins(width, 0, 0, 0);
        holder.itemView.setLayoutParams(params);

        holder.itemView.findViewById(R.id.background).setBackgroundColor(color);

    }

    public void doUnHighlighted(final CommentViewHolder holder, final Comment comment, final CommentNode baseNode) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty() && holder.getAdapterPosition() <= editingPosition) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle("Discard comment?")
                    .setMessage("Do you really want to discard your comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentlyEditing = null;
                            editingPosition = -1;
                            doUnHighlighted(holder, comment, baseNode);
                        }
                    }).setNegativeButton("No", null)
                    .show();
        } else {
            currentlySelected = null;
            currentSelectedItem = "";
            collapse(holder.menuArea);
            int dwidth = (int) (3 * Resources.getSystem().getDisplayMetrics().density);
            int width = 0;

            //Padding on the left, starting with the third comment
            for (int i = 2; i < baseNode.getDepth(); i++) {
                width += dwidth;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(width, 0, 0, 0);
            holder.itemView.setLayoutParams(params);

            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.card_background, typedValue, true);
            int color = typedValue.data;
            holder.itemView.findViewById(R.id.background).setBackgroundColor(color);
        }
    }

    public void doLongClick(final CommentViewHolder holder, final Comment comment, final CommentNode baseNode, final int finalPos, final int finalPos1) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty()) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle("Discard comment?")
                    .setMessage("Do you really want to discard your comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentlyEditing = null;
                            doLongClick(holder, comment, baseNode, finalPos, finalPos1);

                        }
                    }).setNegativeButton("No", null)
                    .show();

        } else {
            if (currentSelectedItem.contains(comment.getFullName())) {
                doUnHighlighted(holder, comment, baseNode);
            } else {
                doHighlighted(holder, comment, baseNode, finalPos, finalPos1);
            }
        }
    }

    public void doOnClick(CommentViewHolder holder, Comment comment, CommentNode baseNode) {
        if (currentSelectedItem.contains(comment.getFullName())) {
            doUnHighlighted(holder, comment, baseNode);
        } else {
            doOnClick(holder, baseNode, comment);
        }
    }

    public void showChildrenObject(final View v) {
        v.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {


            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                v.setAlpha(value);
                v.setScaleX(value);
                v.setScaleY(value);

            }
        });

        animator.start();
    }

    public void hideChildrenObject(final View v) {
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0);
        animator.setDuration(250);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {


            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) (animation.getAnimatedValue())).floatValue();
                v.setAlpha(value);
                v.setScaleX(value);
                v.setScaleY(value);

            }
        });

        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator arg0) {

            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {

                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });

        animator.start();
    }

    public void doOnClick(final CommentViewHolder holder, final CommentNode baseNode, final Comment comment) {
        if (currentlyEditing != null && !currentlyEditing.getText().toString().isEmpty() && holder.getAdapterPosition() <= editingPosition) {
            new AlertDialogWrapper.Builder(mContext)
                    .setTitle("Discard comment?")
                    .setMessage("Do you really want to discard your comment?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentlyEditing = null;
                            editingPosition = -1;
                            doOnClick(holder, baseNode, comment);

                        }
                    }).setNegativeButton("No", null)
                    .show();

        } else {
            if (isClicking) {
                isClicking = false;
                holder.menuArea.removeAllViews();
                isHolder.itemView.findViewById(R.id.menu).setVisibility(View.GONE);
            } else {
                if (hiddenPersons.contains(comment.getFullName())) {
                    unhideAll(baseNode, holder.getAdapterPosition() + 1);
                    hiddenPersons.remove(comment.getFullName());
                    hideChildrenObject(holder.children);
                    holder.firstTextView.setVisibility(View.VISIBLE);
                    holder.commentOverflow.setVisibility(View.VISIBLE);
                } else {
                    int childNumber = getChildNumber(baseNode);
                    if (childNumber > 0) {
                        hideAll(baseNode, holder.getAdapterPosition() + 1);
                        hiddenPersons.add(comment.getFullName());
                        showChildrenObject(holder.children);
                        ((TextView) holder.children).setText("+" + childNumber);
                    }
                    if (holder.firstTextView.getVisibility() == View.VISIBLE && SettingValues.collapseComments) {
                        holder.firstTextView.setVisibility(View.GONE);
                        holder.commentOverflow.setVisibility(View.GONE);
                    } else if (SettingValues.collapseComments) {
                        holder.firstTextView.setVisibility(View.VISIBLE);
                        holder.commentOverflow.setVisibility(View.VISIBLE);
                    }
                }
                clickpos = holder.getAdapterPosition() + 1;
            }
        }
    }

    private int getChildNumber(CommentNode user) {
        int i = 0;
        for (CommentNode ignored : user.walkTree()) {
            i++;
            if (ignored.hasMoreComments()) {
                i++;
            }
        }

        return i - 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || (users != null && users.size() > 0 && position == (users.size() - hidden.size()) + 2) || (users != null && users.size() == 0 && position == 2)) {
            return SPACER;
        } else {
            position -= 1;
        }
        if (position == 0)
            return HEADER;
        return (users.get(getRealPosition(position - 1)) instanceof CommentItem ? 2 : 3);

    }

    @Override
    public int getItemCount() {
        if (users == null) {
            return 2;
        } else {
            return 3 + (users.size() - hidden.size());
        }
    }

    public void unhideAll(CommentNode n, int i) {
        int counter = unhideNumber(n, 0);
        if (SettingValues.collapseComments) {
            listView.setItemAnimator(null);
            notifyItemRangeInserted(i, counter);
        } else {
            listView.setItemAnimator(new ScaleInLeftAnimator());
            listView.setItemAnimator(new FadeInAnimator());
            notifyItemRangeInserted(i, counter);
        }
    }

    public void hideAll(CommentNode n, int i) {

        int counter = hideNumber(n, 0);
        if (SettingValues.collapseComments) {
            listView.setItemAnimator(null);
            notifyItemRangeRemoved(i, counter);
        } else {
            listView.setItemAnimator(new FadeInDownAnimator());
            notifyItemRangeRemoved(i, counter);
        }

    }

    public int unhideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.walkTree()) {
            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {
                String name = ignored.getComment().getFullName();
                if (hiddenPersons.contains(name)) {
                    hiddenPersons.remove(name);
                }
                if (hidden.contains(name)) {
                    hidden.remove(name);
                    i++;
                }
                if (ignored.getMoreChildren() != null) {
                    name = name + "more";
                    if (hiddenPersons.contains(name)) {
                        hiddenPersons.remove(name);
                    }
                    if (hidden.contains(name)) {
                        hidden.remove(name);
                        i++;
                    }
                }
                i += unhideNumber(ignored, 0);
            }
        }
        if (n.hasMoreComments()) {
            String fullname = n.getComment().getFullName() + "more";

            if (hidden.contains(fullname)) {
                i++;
                hidden.remove(fullname);

            }
        }
        return i;
    }

    public int hideNumber(CommentNode n, int i) {
        for (CommentNode ignored : n.walkTree()) {
            if (!ignored.getComment().getFullName().equals(n.getComment().getFullName())) {

                String fullname = ignored.getComment().getFullName();
                if (hiddenPersons.contains(fullname)) {
                    hiddenPersons.remove(fullname);
                }
                if (!hidden.contains(fullname)) {
                    i++;
                    hidden.add(fullname);

                }
                if (ignored.hasMoreComments()) {
                    fullname = fullname + "more";

                    if (!hidden.contains(fullname)) {
                        i++;
                        hidden.add(fullname);

                    }
                }
                i += hideNumber(ignored, 0);
            }
            if (n.hasMoreComments()) {
                String fullname = n.getComment().getFullName() + "more";

                if (!hidden.contains(fullname)) {
                    i++;
                    hidden.add(fullname);

                }
            }
        }
        return i;
    }

    public int getRealPosition(int position) {
        int hElements = getHiddenCountUpTo(position);
        int diff = 0;
        for (int i = 0; i < hElements; i++) {
            diff++;
            if (hidden.contains(users.get(position + diff).getName())) {
                i--;
            }
        }
        return (position + diff);
    }

    private int getHiddenCountUpTo(int location) {
        int count = 0;
        for (int i = 0; i <= location; i++) {
            if (hidden.contains(users.get(i).getName()))
                count++;
        }
        return count;
    }

    public class AsyncLoadMore extends AsyncTask<MoreChildItem, Void, Integer> {
        public MoreCommentViewHolder holder;
        public int holderPos;
        public int position;

        public AsyncLoadMore(int position, int holderPos, MoreCommentViewHolder holder) {
            this.holderPos = holderPos;
            this.holder = holder;
            this.position = position;
        }

        @Override
        public void onPostExecute(Integer data) {
            listView.setItemAnimator(new ScaleInLeftAnimator());

            notifyItemRangeInserted(holderPos, data);

            currentPos = holderPos;
            toShiftTo = ((LinearLayoutManager) listView.getLayoutManager()).findLastVisibleItemPosition();
            shiftFrom = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
        }

        @Override
        protected Integer doInBackground(MoreChildItem... params) {

            ArrayList<CommentObject> finalData = new ArrayList<>();
            int i = 0;

            if (params.length > 0) {
                try {
                    CommentNode node = params[0].comment;
                    node.loadMoreComments(Authentication.reddit);
                    HashMap<Integer, MoreChildItem> waiting = new HashMap<>();

                    for (CommentNode n : node.walkTree()) {
                        if (!keys.containsKey(n.getComment().getFullName())) {

                            CommentObject obj = new CommentItem(n);
                            ArrayList<Integer> removed = new ArrayList<>();
                            Map<Integer, MoreChildItem> map = new TreeMap<>(Collections.reverseOrder());
                            map.putAll(waiting);

                            for (Integer i2 : map.keySet()) {
                                if (i2 >= n.getDepth()) {
                                    finalData.add(waiting.get(i2));
                                    removed.add(i2);
                                    waiting.remove(i2);
                                    i++;

                                }
                            }

                            finalData.add(obj);
                            i++;

                            if (n.hasMoreComments()) {
                                waiting.put(n.getDepth(), new MoreChildItem(n, n.getMoreChildren()));
                            }
                        }
                    }
                    if (node.hasMoreComments()) {
                        finalData.add(new MoreChildItem(node, node.getMoreChildren()));
                        i++;
                    }
                } catch (Exception e) {
                    Log.w(LogUtil.getTag(), "Cannot load more comments " + e);
                }

                shifted += i;
                users.remove(position);
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemRemoved(holderPos);
                    }
                });
                users.addAll(position, finalData);

                for (int i2 = 0; i2 < users.size(); i2++) {
                    keys.put(users.get(i2).getName(), i2);
                }
            }
            return i;
        }
    }

    public class ReplyTaskComment extends AsyncTask<String, Void, String> {
        public Contribution sub;
        int finalPos;
        int finalPos1;
        CommentNode node;

        public ReplyTaskComment(Contribution n, int finalPos, int finalPos1, CommentNode node) {
            sub = n;
            this.finalPos = finalPos;
            this.finalPos1 = finalPos1;
            this.node = node;
        }

        public ReplyTaskComment(Contribution n) {
            sub = n;
        }

        @Override
        public void onPostExecute(final String s) {
            if (s != null) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        ((Activity) mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dataSet.refreshLayout.setRefreshing(false);

                                dataSet.loadMore(CommentAdapter.this, submission.getSubredditName());
                                currentSelectedItem = s;
                            }
                        });
                    }
                }, 2000);
            }
        }

        @Override
        protected String doInBackground(String... comment) {
            if (Authentication.me != null) {
                try {
                    return new AccountManager(Authentication.reddit).reply(sub, comment[0]);
                } catch (ApiException e) {
                    Log.v(LogUtil.getTag(), "UH OH!!");
                    //todo this
                }
            }
            return null;
        }
    }

    public void showBottomSheet(final Context mContext, final CommentViewHolder holder, final Comment n) {

        int[] attrs = new int[]{R.attr.tint};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        int color = ta.getColor(0, Color.WHITE);
        Drawable profile = mContext.getResources().getDrawable(R.drawable.profile);
        Drawable saved = mContext.getResources().getDrawable(R.drawable.iconstarfilled);
        Drawable gild = mContext.getResources().getDrawable(R.drawable.gild);
        Drawable copy = mContext.getResources().getDrawable(R.drawable.ic_content_copy);
        Drawable share = mContext.getResources().getDrawable(R.drawable.share);

        profile.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        saved.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        gild.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        copy.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        share.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        BottomSheet.Builder b = new BottomSheet.Builder((Activity) mContext)
                .title(Html.fromHtml(n.getBody()));


        b.sheet(1, profile, "/u/" + n.getAuthor());
        String save = mContext.getString(R.string.btn_save);
        if (ActionStates.isSaved(n)) {
            save = mContext.getString(R.string.comment_unsave);
        }
        if (Authentication.isLoggedIn)
            b.sheet(3, saved, save);
        b.sheet(5, gild, mContext.getString(R.string.comment_gild))
                .sheet(7, copy, mContext.getString(R.string.submission_copy))
                .sheet(4, share, mContext.getString(R.string.comment_share))
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 1: {
                                Intent i = new Intent(mContext, Profile.class);
                                i.putExtra(Profile.EXTRA_PROFILE, n.getAuthor());
                                mContext.startActivity(i);
                            }
                            break;

                            case 3:
                                if (ActionStates.isSaved(n)) {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            try {
                                                ActionStates.setSaved(n, false);
                                                new AccountManager(Authentication.reddit).unsave(n);
                                            } catch (ApiException e) {
                                                e.printStackTrace();
                                            }

                                            return null;
                                        }

                                    }.execute();


                                } else {
                                    new AsyncTask<Void, Void, Void>() {
                                        @Override
                                        protected Void doInBackground(Void... params) {
                                            try {
                                                ActionStates.setSaved(n, true);
                                                new AccountManager(Authentication.reddit).save(n);
                                            } catch (ApiException e) {
                                                e.printStackTrace();
                                            }

                                            return null;
                                        }

                                    }.execute();

                                }

                                break;
                            case 5: {
                                Intent i = new Intent(mContext, Website.class);
                                i.putExtra(Website.EXTRA_URL, "https://reddit.com" + submission.getPermalink() +
                                        n.getFullName().substring(3, n.getFullName().length()) + "?context=3");
                                i.putExtra(Website.EXTRA_COLOR, Palette.getColor(n.getSubredditName()));
                                mContext.startActivity(i);
                            }
                            break;
                            case 7:
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Comment text", n.getBody());
                                clipboard.setPrimaryClip(clip);

                                Toast.makeText(mContext, "Comment text copied", Toast.LENGTH_SHORT).show();
                                break;
                            case 4:
                                Reddit.defaultShareText("https://reddit.com" + submission.getPermalink() +
                                        n.getFullName().substring(3, n.getFullName().length()) + "?context=3"
                                        , mContext);
                                break;
                        }
                    }
                });


        b.show();
    }

}