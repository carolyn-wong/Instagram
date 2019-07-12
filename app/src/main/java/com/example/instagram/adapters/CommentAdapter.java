package com.example.instagram.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.instagram.R;
import com.example.instagram.UserTimelineActivity;
import com.example.instagram.models.Comment;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private List<Comment> comments;
    Context context;

    // pass Comment array in constructor
    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    // for each row, inflate layout and cache references into ViewHolder
    // method invoked only when creating a new row
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View commentView = inflater.inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(commentView);
    }

    // bind values based on element position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get data according to position
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // create ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvCommentUsername)
        public TextView tvUsername;
        @BindView(R.id.tvBody)
        public TextView tvDescription;

        // constructor takes in inflated layout
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final Comment comment) {
            // populate views according to data
            tvUsername.setText(comment.getUser().getUsername());
            tvDescription.setText(comment.getBody());

            View.OnClickListener userClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ParseUser user = comment.getUser();
                    Intent intent = new Intent(context, UserTimelineActivity.class);
                    intent.putExtra("user", user);
                    context.startActivity(intent);
                }
            };

            tvUsername.setOnClickListener(userClickListener);
        }
    }

    // RecyclerView adapter helper methods to clear items from or add items to underlying dataset
    // clean recycler elements
    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    // add list of comments - change list type depending on item type used
    public void addAll(List<Comment> list) {
        comments.addAll(list);
        notifyDataSetChanged();
    }
}
