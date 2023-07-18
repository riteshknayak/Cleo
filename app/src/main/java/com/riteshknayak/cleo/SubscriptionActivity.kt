package com.riteshknayak.cleo

import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.riteshknayak.cleo.Adapters.ItemAdapter
import com.riteshknayak.cleo.Models.SubscriptionDataset
import com.riteshknayak.cleo.Utils.Security
import com.riteshknayak.cleo.databinding.ActivitySubscriptionBinding
import java.io.IOException
import java.util.concurrent.Executors

class SubscriptionActivity : AppCompatActivity() {
    private lateinit var itemArraylist:ArrayList<SubscriptionDataset>
    private var isSuccess =false
    var productId = 0
    var auth: FirebaseAuth? = null
    var uid: String? = null

    var database: FirebaseFirestore? = null

    private var billingClient:BillingClient? = null
    lateinit var binding: ActivitySubscriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = Firebase.firestore

        //Initialise Firebase Auth and get uid
        auth = Firebase.auth
        val firebaseUser = auth!!.currentUser!!
        uid = firebaseUser.uid


        binding.premiumRecyclerView.layoutManager= LinearLayoutManager(this)
        binding.premiumRecyclerView.hasFixedSize()
        itemArraylist = arrayListOf<SubscriptionDataset>()

        billingClient =BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        showList()

    }
    private val purchasesUpdatedListener= PurchasesUpdatedListener {
            billingResult, purchases ->
        if(billingResult.responseCode== BillingClient.BillingResponseCode.OK && purchases != null){
            for(purchase in purchases){
                handlePurchase(purchase)

            }
        }else if(billingResult.responseCode ==BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            Toast.makeText(this,"Already Subscribed", Toast.LENGTH_SHORT).show()
            isSuccess = true


            if (auth == null){
                //Initialise Firebase Auth and get uid
                auth = Firebase.auth
                val firebaseUser = auth!!.currentUser!!
                uid = firebaseUser.uid
            }

            val creditsData1: MutableMap<String, Any> = HashMap()
            creditsData1["premiumUser"] = true

            val creditsData = hashMapOf("premiumUser" to true,)
            database!!.collection("users").document("SF").set(creditsData)


        }else if (billingResult.responseCode == BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED) {
            Toast.makeText(this,"Feature Not Supported", Toast.LENGTH_SHORT).show()
        }else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Toast.makeText(this,"Cancelled", Toast.LENGTH_SHORT).show()
        }else if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Toast.makeText(this,"Billing Unavailable", Toast.LENGTH_SHORT).show()
        }else if (billingResult.responseCode == BillingClient.BillingResponseCode.NETWORK_ERROR) {
            Toast.makeText(this,"Network Error", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Error " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private fun handlePurchase(purchase: Purchase){
        val consumeParams =ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener{billingResult, s ->
            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK){

            }
        }

        billingClient!!.consumeAsync(consumeParams,listener)
        if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED){

            if(!verifyValidSignature(purchase.originalJson,purchase.signature)){
                Toast.makeText(this,"Invalid Purchase",Toast.LENGTH_SHORT).show()
                return
            }
            if(!purchase.isAcknowledged){
                val acknowledgePurchaseParams =AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient!!.acknowledgePurchase(
                    acknowledgePurchaseParams,acknowledgePurchaseResponseListener)
                isSuccess = true
                if (auth == null){
                    //Initialise Firebase Auth and get uid
                    auth = Firebase.auth
                    val firebaseUser = auth!!.currentUser!!
                    uid = firebaseUser.uid
                }

                val creditsData1: MutableMap<String, Any> = HashMap()
                creditsData1["premiumUser"] = true

                val creditsData = hashMapOf("premiumUser" to true,)
                database!!.collection("users").document("SF").set(creditsData)



            }else{
                Toast.makeText(this,"Already Subscribe",Toast.LENGTH_SHORT).show()
                Toast.makeText(this,"Already Subscribe",Toast.LENGTH_SHORT).show()
            }
        }else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Toast.makeText(this,"PENDING",Toast.LENGTH_SHORT).show()
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Toast.makeText(this,"UNSPECIFIED STATE",Toast.LENGTH_SHORT).show()
        }
    }

    private var acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener{ billingResult ->
        if(billingResult.responseCode ==BillingClient.BillingResponseCode.OK){
            isSuccess = true
            if (auth == null){
                //Initialise Firebase Auth and get uid
                auth = Firebase.auth
                val firebaseUser = auth!!.currentUser!!
                uid = firebaseUser.uid
            }

            val creditsData1: MutableMap<String, Any> = HashMap()
            creditsData1["premiumUser"] = true

            val creditsData = hashMapOf("premiumUser" to true,)
            database!!.collection("users").document("SF").set(creditsData)


        }
    }
    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val security = Security()
            val base64Key ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgvmhPdPKCRBiJfqQFOZJVXU+lV4LYBwkCgFcIpKyF1QV5uVG5bHfTfrNXrNpfpJRy+z+kh1jA/mcA7U8WhSkEL3iisjeV0vg+cjQWOISrlok1Ihfv82j6dvpmKZ6aEfHLR4nlWfNK4n0cEMbKGMp0+Fhstp3r1eQvc6BsK7pOjrnB7cXpMwFtlc+TU/UXYKUeLPb5onMwWVC9kQCJn6taJ60wlQufenY4+T5BNkHgpZiCAjPahQpMJTB4lAk51o2M/+YfN+hz0fYcLpzn/PmmOVUAe5+J6PqUECwb1uv98bFWL1o+t8sYURtxpROF302cJhe3ROEoNFdCFEjfuRyVwIDAQAB"
            security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }


    private  fun showList(){
        billingClient!!.startConnection(object :BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
                TODO("Not yet implemented")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val executorService = Executors.newSingleThreadExecutor()
                executorService.execute{
                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("premium")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                    billingClient!!.queryProductDetailsAsync(params.build()){
                            billingResult,productDetailsList ->
                        for(productDetails in productDetailsList){
                            if(productDetails.subscriptionOfferDetails != null){
                                for (i in 0 until productDetails.subscriptionOfferDetails!!.size) {
                                    var subsName:String = productDetails.name
                                    var index:Int = i
                                    var phases =""
                                    var formattedPrice: String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice.toString()
                                    var billingPeriod: String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.billingPeriod.toString()
                                    var recurrenceMode:String =productDetails.subscriptionOfferDetails?.get(i)
                                        ?.pricingPhases?.pricingPhaseList?.get(0)?.recurrenceMode.toString()
                                    if(recurrenceMode == "2"){
                                        when(billingPeriod){
                                            "P1M"-> billingPeriod =" For 1 Month"
                                            "P1Y"-> billingPeriod =" For 1 Year"
                                            "P1W"-> billingPeriod =" For 1 Week"
                                        }
                                    }else{
                                        when(billingPeriod){
                                            "P1M"-> billingPeriod ="/Month"
                                            "P1Y"-> billingPeriod ="/Year"
                                            "P1W"-> billingPeriod ="/Week"
                                        }
                                    }
                                    phases ="$formattedPrice$billingPeriod"
                                    for (j in 0 until (productDetails.subscriptionOfferDetails!![i]?.pricingPhases?.pricingPhaseList?.size!!)) {
                                        if(j>0){
                                            var period: String = productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases
                                                ?.pricingPhaseList?.get(j)?.billingPeriod.toString()
                                            var price: String = productDetails.subscriptionOfferDetails?.get(i)?.pricingPhases
                                                ?.pricingPhaseList?.get(j)?.formattedPrice.toString()
                                            when(period){
                                                "P1M"-> period ="/Month"
                                                "P1Y"-> period ="/Year"
                                                "P1W"-> period ="/Week"
                                            }
                                            subsName +="\n"+productDetails.subscriptionOfferDetails?.get(i)?.offerId.toString()
                                            phases += "\n$price$period"
                                        }
                                    }
                                    val tmpItm = SubscriptionDataset(subsName,phases,index)
                                    itemArraylist.add(tmpItm)
                                }

                            }
                        }

                    }

                }
                runOnUiThread {
                    try {
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    var adapter = ItemAdapter(itemArraylist)
                    binding.premiumRecyclerView.adapter = adapter
                    adapter.setOnItemClickListener(object :ItemAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val cItem =itemArraylist[position]
                            productId =cItem.planIndex
                            subscribeProduct()
                        }

                    })

                }
            }

        })
    }
    fun subscribeProduct(){
        billingClient!!.startConnection(object :BillingClientStateListener{
            override fun onBillingServiceDisconnected() {
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if(billingResult.responseCode== BillingClient.BillingResponseCode.OK){
                    val productList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("premium")
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                    billingClient!!.queryProductDetailsAsync(params.build()) {
                            billingResult, productDetailsList ->
                        for (productDetails in productDetailsList) {
                            val offerToken = productDetails.subscriptionOfferDetails?.get(productId)?.offerToken
                            val productDetailsParamsList = listOf(
                                offerToken?.let {
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .setOfferToken(it)
                                        .build()
                                }
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            val billingResult = billingClient!!.launchBillingFlow(this@SubscriptionActivity,billingFlowParams)

                        }
                    }

                }

            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

}