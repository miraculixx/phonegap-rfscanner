//
//  ScanService.h
//  Geolocation
//
//  Created by dev on 2/9/16.
//
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <Cordova/CDVPlugin.h>
#import <CoreLocation/CoreLocation.h>
#import "sqlite3.h"

@interface ScanService : CDVPlugin<CLLocationManagerDelegate>{
    sqlite3 *contactDB;
    NSString *dataBasePath;
    CLLocation* location;
    NSDateFormatter *DateFormatter;
    NSString *EnterRegion;
    NSString *JSONString;
    NSMutableArray *JSONArray;
    int m_timeStamp;
    NSDate* now;
    NSDate* now1;
    NSString* coord;
}
@property(strong, nonatomic) CLLocationManager *locationManager;
@property(strong, nonatomic) CLCircularRegion *geoRegion;

-(void)CreateDataBase;
-(void)InsertData;
-(void)MakeJSON_Make;
-(void)postJSON;
-(void)regionStart;
-(void)InitData;

- (void)start:(CDVInvokedUrlCommand *)command;
- (void)stop:(CDVInvokedUrlCommand *)command;
@end
