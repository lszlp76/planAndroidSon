package com.zlpls.plantinsta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zlpls.plantinsta.visualselection.FileOperations;

import java.util.Random;

//1
public class AddPlantAdapter extends FirestoreRecyclerAdapter<PlantModel, AddPlantAdapter.PlantHolder> {
    private OnItemClickListener listener;
    private OnItemLongClickListener longlistener;
    FileOperations fileOperations = new FileOperations();

    public AddPlantAdapter(@NonNull FirestoreRecyclerOptions<PlantModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull PlantHolder holder, int position, @NonNull PlantModel model) {
        holder.mplantNameText.setText(model.getPlantName());
        holder.mplantFirstDate.setText(model.getplantFirstDate());
        holder.mplantPostCount.setText(model.getPlantPostCount()+" adet günceniz var");
        //holder.mplantPostNumber.setText(model.getPlantAvatar());

        Random r = new Random();
        int low = 300;
        int high = 700;
        int result = r.nextInt(high-low) + low;
       double width = (result*1.6);
        //int degree = fileOperations.getImageRotationbyPath(model.getPlantAvatar());

            Picasso.get().load(String.valueOf(model.getPlantAvatar()))


                    .resize(500,(result))
                    .into(holder.mplantAvatarPicture);


        //holder.plantPostNumber.setText(model.getPlantPostNumber());

    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position)
                .getReference().delete();

    }


    @NonNull
    @Override
    public PlantHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item,
                parent, false);
        return new PlantHolder(v);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;

    }
    public void setOnItemLongClickListener(OnItemLongClickListener longlistener){
        this.longlistener=longlistener;
    }
    public interface OnItemClickListener {

        void onItemClick(DocumentSnapshot documentSnapshot, int position);
        void onDelete(DocumentSnapshot documentSnapshot,int position);
    }
    public interface OnItemLongClickListener{
        void onItemLongClick(DocumentSnapshot documentSnapshot,int position);
        void onDelete(DocumentSnapshot documentSnapshot,int position);
    }
    //2
    class PlantHolder extends RecyclerView.ViewHolder {
        TextView mplantNameText;
        TextView mplantFirstDate;
        ImageView mplantAvatarPicture;
        TextView mplantPostCount;
        ImageView mGarbage;
        //TextView mplantPostNumber;

        //TextView plantPostNumber;
        public PlantHolder(@NonNull View itemView) {
            super(itemView);
            mplantNameText = itemView.findViewById(R.id.plantnametext);
            mplantFirstDate = itemView.findViewById(R.id.plantfirstdate);
            mplantAvatarPicture = itemView.findViewById(R.id.plantavatarpicture);
            mplantPostCount = itemView.findViewById(R.id.plantpostnumber);
           // mGarbage = itemView.findViewById(R.id.garbageicon);
            // mplantPostNumber= itemView.findViewById(R.id.plantpostnumber);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
           /*
            mGarbage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onDelete(getSnapshots().getSnapshot(position), position);
                    }
                }

            });
            */

            // özellik eklemek için mutlaka izle.
            // https://codinginflow.com/tutorials/android/simple-recyclerview-java/part-5-onclicklistener-single-view
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && longlistener != null) {
                        longlistener.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    }

                    return false;
                }
            });

        }
    }
}

/*
 private String plantAvatar;
    private String plantDate;
    private  String plantUserMail;
class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPriority;
        public NoteHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);
        }
    }
 */