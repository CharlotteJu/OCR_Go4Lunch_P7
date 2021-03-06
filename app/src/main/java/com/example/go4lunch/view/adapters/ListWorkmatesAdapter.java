package com.example.go4lunch.view.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.go4lunch.R;
import com.example.go4lunch.model.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ListWorkmatesAdapter extends RecyclerView.Adapter<ListWorkmatesAdapter.ListWorkmatesViewHolder>   //FirestoreRecyclerAdapter<User, ListWorkmatesAdapter.ListWorkmatesViewHolder>
{

    private RequestManager glide;
    private Context context;
    private Activity activity;
    private List<User> usersList;
    private OnClickListenerItemList onClickListenerItemList;

    public ListWorkmatesAdapter(RequestManager glide, Activity activity, OnClickListenerItemList onClickListenerItemList)
    {
        this.glide = glide;
        this.activity = activity;
        this.onClickListenerItemList = onClickListenerItemList;
        this.usersList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ListWorkmatesAdapter.ListWorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        this.context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.item_list_workmates, parent, false);
        return new ListWorkmatesAdapter.ListWorkmatesViewHolder(v, this.activity, this.onClickListenerItemList);
    }

    @Override
    public void onBindViewHolder(@NonNull ListWorkmatesViewHolder holder, int position)
    {
        holder.updateUI(this.usersList.get(position), this.glide, this.context);
    }

    @Override
    public int getItemCount()
    {
        return this.usersList.size();
    }

    public void updateList(List<User> userList)
    {
        this.usersList = userList;
        this.notifyDataSetChanged();
    }

    static class ListWorkmatesViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.item_list_workmates_image)
        ImageView imageView;
        @BindView(R.id.item_list_workmates_txt)
        TextView textView;
        private Activity activity;
        private OnClickListenerItemList onClickListenerItemList;

        private ListWorkmatesViewHolder(@NonNull View itemView, Activity activity, OnClickListenerItemList onClickListenerItemList)
        {
            super(itemView);
            ButterKnife.bind(this,itemView);
            this.activity = activity;
            this.onClickListenerItemList = onClickListenerItemList;
        }

        /**
         * Update textView with information from Firebase
         * If the user chose a restaurant, textView is bold
         */
        private void updateUI(User user, RequestManager glide, Context context)
        {
            glide.load(user.getIllustration()).apply(RequestOptions.circleCropTransform()).into(imageView);
            String textString;
            String finalText;
            String firstName = user.getName().split(" ")[0];

            if (user.isChooseRestaurant()) {

                textString = activity.getResources().getString(R.string.list_workmates_adapter_is_eating);
                String textStringEnd = activity.getResources().getString(R.string.list_workmates_adapter_parenthesis);

                finalText = firstName + " " + textString + user.getRestaurantChoose().getName() + textStringEnd;

                textView.setText(finalText);

                if (Build.VERSION.SDK_INT < 23) {
                    textView.setTextAppearance(context, R.style.item_list_workmates_choose_txt);
                } else {
                    textView.setTextAppearance(R.style.item_list_workmates_choose_txt);
                }
            }
            else
            {
                textString = " " + activity.getResources().getString(R.string.list_workmates_adapter_hasnt_decided_yed);
                finalText = firstName + textString;
                textView.setText(finalText);

                if (Build.VERSION.SDK_INT < 23) {
                    textView.setTextAppearance(context, R.style.item_list_workmates_no_choose_txt);
                } else {
                    textView.setTextAppearance(R.style.item_list_workmates_no_choose_txt);
                }
            }
        }

        @OnClick(R.id.item_list_workmates_card_view)
        void onClickItem() { onClickListenerItemList.onClickListener(getAdapterPosition());}

    }
}
