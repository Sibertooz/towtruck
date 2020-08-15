package nways.towtruck.user;

public class Provider {
    private String providerName, providerPhone, providerEmail;
    private double providerLat, providerLng;

    Provider(String providerName, String providerPhone, String providerEmail,
             double providerLat, double providerLng){
        this.providerName = providerName;
        this.providerPhone = providerPhone;
        this.providerEmail = providerEmail;
        this.providerLat = providerLat;
        this.providerLng = providerLng;
    }

    public void setProviderName(String providerName){
        this.providerName = providerName;
    }

    public void setProviderPhone(String providerPhone){
        this.providerPhone = providerPhone;
    }

    public void setProviderEmail(String providerEmail){
        this.providerEmail = providerEmail;
    }

    public void setProviderLat(double providerLat){
        this.providerLat = providerLat;
    }

    public void setProviderLng(double providerLng){
        this.providerLng = providerLng;
    }

    public String getProviderName(){
        return providerName;
    }

    public String getProviderEmail(){
        return providerEmail;
    }

    public String getProviderPhone(){
        return providerPhone;
    }

    public double getProviderLat(){
        return providerLat;
    }

    public double getProviderLng() {
        return providerLng;
    }
}
