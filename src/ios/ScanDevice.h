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

@interface ScanDevice : CDVPlugin<CLLocationManagerDelegate>{
    sqlite3 *contactDB;
    NSTimer *time_interval;
    
    NSString *dataBasePath;
    NSString *EnterRegion;
    NSString *JSONString;
    NSString *multiCoord;
    NSString *url;
    
    CLLocation* location;
    NSDateFormatter *DateFormatter;
    
    NSMutableArray *JSONArray;
    
    NSDate* initNow;
    
    int m_timeStamp;
    BOOL loopflag;
    
}
@property(strong, nonatomic) CLLocationManager *locationManager;
@property(strong, nonatomic) CLCircularRegion *geoRegion;
@property(nonatomic, strong) NSThread *thread;

-(void)createDataBase;
-(void)insertData;
-(void)makeJSON_Data;
-(void)postJSON:(NSMutableArray*)data;
-(void)regionStart:(NSString*)str_array;
-(void)insertData:(float)lat lon:(float)lon alt:(float)alt currentTime:(NSDateFormatter*)currentTime region:(NSString*)region identfier:(NSString*)identfier;
-(void)startTimedTask;

- (void)start:(CDVInvokedUrlCommand *)command;
- (void)stop:(CDVInvokedUrlCommand *)command;
@end
