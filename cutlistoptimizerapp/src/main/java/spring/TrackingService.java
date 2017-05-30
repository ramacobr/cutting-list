package spring;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
public class TrackingService {

    @Autowired
    private ApplicationContext context;

    private DatabaseReader cityDatabaseReader;

    private DatabaseReader asnDatabaseReader;

    @PostConstruct
    public void init() {
        try {
            InputStream cityDatabaseFile = context.getResource("classpath:GeoLite2-City_20170502/GeoLite2-City.mmdb").getInputStream();
            cityDatabaseReader = new DatabaseReader.Builder(cityDatabaseFile).build();

            InputStream asnDatabaseFile = context.getResource("classpath:GeoLite2-ASN_20170516/GeoLite2-ASN.mmdb").getInputStream();
            asnDatabaseReader = new DatabaseReader.Builder(asnDatabaseFile).build();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TrackingDataModel getTrackingData(String ipAddr) {

        TrackingDataModel trackingDataModel = new TrackingDataModel();

        try {
            InetAddress ipAddress = InetAddress.getByName(ipAddr);

            CityResponse cityResponse = cityDatabaseReader.city(ipAddress);
            trackingDataModel.setCountry(cityResponse.getCountry().getName());
            trackingDataModel.setCity(cityResponse.getCity().getName());

            AsnResponse ansResponse = asnDatabaseReader.asn(ipAddress);
            trackingDataModel.setOrganisation(ansResponse.getAutonomousSystemOrganization());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return trackingDataModel;
    }
}
