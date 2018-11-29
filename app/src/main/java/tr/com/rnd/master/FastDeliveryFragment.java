package tr.com.rnd.master;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import tr.com.rnd.master.Adapters.FastDeliveryAdapter;
import tr.com.rnd.master.Model.Request.FastDeliveryIdsRequest;
import tr.com.rnd.master.Model.Result.FastDeliveryIDSResult;
import tr.com.rnd.master.Model.interfaces.ShowProgress;

public class FastDeliveryFragment extends Fragment implements ShowProgress {
    View progress;

    RecyclerView table;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fast_delivery, container, false);

        ((BasketActivity) getActivity()).toolbar.setNavigationIcon(R.drawable.back);
        ((BasketActivity) getActivity()).toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        progress = view.findViewById(R.id.progress);

        table = (RecyclerView) view.findViewById(R.id.table);
        table.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        table.setHasFixedSize(true);

        showProgress(true);

        final FastDeliveryIdsRequest request = new FastDeliveryIdsRequest(((BasketActivity)getActivity()).tokenResult.tokenType, ((BasketActivity)getActivity()).tokenResult.accessToken);
        Log.d("TAG", "getOrder: " + request.getHeaders());
        Log.d("TAG", "getOrder: " + request.getParameters());
        ((BasketActivity) getActivity()).fastDeliverys = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request.method, request.getURL(), request.getParameters(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showProgress(false);

                Log.d("TAG", response.toString());

                Gson gson = new Gson();
                FastDeliveryIDSResult.FastDeliveryIDS responseObject = gson.fromJson(response.toString(), FastDeliveryIDSResult.FastDeliveryIDS.class);

                Log.d("TAG", "response: " + responseObject.success);
                if (responseObject.success) {
                    ((BasketActivity) getActivity()).fastDeliverys.add(new FastDelivery("Standart Teslimat", "Standart teslimat MNG Kargo ile 2-3 iş günü içerisinde teslim edilir."));

                    String[] separated = responseObject.data.split(",");
                    if (Arrays.asList(separated).contains(String.valueOf(((BasketActivity) getActivity()).selectAreaId))) {
                        ((BasketActivity) getActivity()).fastDeliverys.add(new FastDelivery("Aynı Gün Teslimat", "11:00 öncesi siparişleriniz 18:00'a kadar teslim edilir. 11:00 sonrası siparişleriniz ertesi gün işlem görür."));
                    }
                }

                table.setAdapter(new FastDeliveryAdapter(((BasketActivity) getActivity()).fastDeliverys, fastDeliveryAdapterListener));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false);

                Log.d("TAG", "onErrorResponse: " + String.valueOf(error.getMessage()));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return request.getHeaders();
            }
        };
        queue.add(jsonObjectRequest);

        return view;
    }

    FastDeliveryAdapter.FastDeliveryAdapterListener fastDeliveryAdapterListener = new FastDeliveryAdapter.FastDeliveryAdapterListener() {
        @Override
        public void fastDeliveryAdapterListener(final int position) {
            Log.d("TAG", "fastDeliveryAdapterListener: " + position);
            ((BasketActivity) getActivity()).selectFastDeliveryIndex = position;

            ((BasketActivity) getActivity()).loadFragment(new PaymentTypesFragment());
        }
    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow for very easy animations. If available, use these APIs to fade-in the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progress.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    public class FastDelivery {
        public String name, note;

        public FastDelivery(String name, String note) {
            this.name = name;
            this.note = note;
        }
    }
}
