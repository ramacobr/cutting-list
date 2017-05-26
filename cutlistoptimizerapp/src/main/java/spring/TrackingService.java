package spring;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CityResponse;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

@Service
public class TrackingService {

    private DatabaseReader cityDatabaseReader;

    private DatabaseReader asnDatabaseReader;

    @PostConstruct
    public void init() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            File cityDatabaseFile = new File(classLoader.getResource("GeoLite2-City_20170502/GeoLite2-City.mmdb").getFile());
            cityDatabaseReader = new DatabaseReader.Builder(cityDatabaseFile).build();

            File asnDatabaseFile = new File(classLoader.getResource("GeoLite2-ASN_20170516/GeoLite2-ASN.mmdb").getFile());
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
