//
//  GeolocationPlugin.m
//  Geolocation
//
//  Created by dev on 2/9/16.
//
//

#import "GeolocationPlugin.h"

@implementation GeolocationPlugin

-(void)start:(CDVInvokedUrlCommand *)command
{
    // NSString *callbackId = command.callbackId;
    //  [self returnLocationInfo:callbackId andKeepCallback:"OKay"];
    
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];
    
    [self InitData];

    
}

-(void)stop:(CDVInvokedUrlCommand *)command
{
    
    
}

-(void)InitData{
    [self CreateDataBase];
    EnterRegion = @"NO";
    // Add location Manager class
    if (nil == self.locationManager) {
        self.locationManager = [[CLLocationManager alloc] init];
        // Get version of iOS
        if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0) {
            
            [ self.locationManager requestWhenInUseAuthorization];
            
            [self.locationManager requestAlwaysAuthorization];
        }
    }
    now = [NSDate date];
    now1 = [NSDate date];
    
    if (nil == self.locationManager)
        self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    
    
    [self.locationManager startMonitoringSignificantLocationChanges];
    [self.locationManager startUpdatingLocation];
    [self regionStart];
}


// recieve GPS updated location
- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    
    
    location = [locations lastObject];
    
    NSDate* timestamp = location.timestamp;
    
    
    
    NSTimeInterval age = [NSDate.date timeIntervalSinceDate:location.timestamp];
    
    if (fabs([now timeIntervalSinceDate:timestamp]) < 60){
        
        NSLog(@"Time 60 second print");
        
        if (fabs([now1 timeIntervalSinceDate:timestamp]) > 15) {

            
            DateFormatter = [[NSDateFormatter alloc] init];
            [DateFormatter setDateFormat:@"yyyyMMdd'T'HH:mm:ss"];
            [self InsertData];
        }
        
    }
    else{
        NSLog(@"Time 60 more than print");
        [self MakeJSON_Make ];
        [self stopUpdatingLocationWithMessage:NSLocalizedString(@"Error", @"Error")];
        
    }
}



- (void)stopUpdatingLocationWithMessage:(NSString *)state {
    
    [self.locationManager stopUpdatingLocation];
    self.locationManager.delegate = nil;
}

-(void)regionStart{
    CLLocationCoordinate2D center = CLLocationCoordinate2DMake(40.025129, 124.343749);
    CLRegion *region = [[CLCircularRegion alloc] initWithCenter:center radius:0.00001 identifier:@"Center"];
    
    [self.locationManager startMonitoringForRegion:region];
}

-(void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region{
    
    //[self MakeJSON_Make];
    
    NSLog(@"Now monitoring for %@", region.identifier);
    NSLog(@"Welcome to %f", region.radius);
    NSString *message = @"Show Message...\n didStartMonitoringForRegion";
    UIAlertView *toast = [[UIAlertView alloc]initWithTitle:nil message:message delegate:nil cancelButtonTitle:nil otherButtonTitles:nil, nil];
    
    [toast show];
    int duration = 1;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration *NSEC_PER_SEC), dispatch_get_main_queue(), ^{[toast dismissWithClickedButtonIndex:0 animated:YES];
    });
}
-(void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region{
    EnterRegion = @"YES";
    
    NSString *message = @"Show Message...\n Called didEnterRegion function";
    UIAlertView *toast = [[UIAlertView alloc]initWithTitle:nil message:message delegate:nil cancelButtonTitle:nil otherButtonTitles:nil, nil];
    
    [toast show];
    int duration = 1;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration *NSEC_PER_SEC), dispatch_get_main_queue(), ^{[toast dismissWithClickedButtonIndex:0 animated:YES];
    });
    
}
-(void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region{
    EnterRegion = @"NO";
    
    NSString *message = @"Show Message...\n Called didExitRegion function";
    UIAlertView *toast = [[UIAlertView alloc]initWithTitle:nil message:message delegate:nil cancelButtonTitle:nil otherButtonTitles:nil, nil];
    
    [toast show];
    int duration = 1;
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, duration *NSEC_PER_SEC), dispatch_get_main_queue(), ^{[toast dismissWithClickedButtonIndex:0 animated:YES];
    });
}

-(void)CreateDataBase{
    
    //Get the documents directory
    NSArray *dirPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *docsDir = [dirPaths objectAtIndex:0];
    
    //Build the path to the database file
    dataBasePath = [[NSString alloc]initWithString:[docsDir stringByAppendingPathComponent:@"GPS_Track.sqlite"]];
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    
    
    if ([fileManager fileExistsAtPath:dataBasePath] == NO) {
        const char *dbpath = [dataBasePath UTF8String];
        
        if (sqlite3_open(dbpath, &contactDB) == SQLITE_OK) {
            char *errMsg;
            
            const char *sql_table = "CREATE TABLE GPSTable(_id INTEGER PRIMARY KEY NOT NULL, latitude TEXT, longitude TEXT, altitude TEXT, timerInterval TEXT, region TEXT);";
            
            if (sqlite3_exec(contactDB, sql_table, NULL, NULL, &errMsg) != SQLITE_OK) {
                NSLog(@"Failed to create table");
            }
            sqlite3_close(contactDB);
        }
        else{
            NSLog(@"Failed to open/create databese");
        }
    }
}

-(void)InsertData{
    sqlite3_stmt    *statement;
    
    const char *dbpath = [dataBasePath UTF8String];
    
    if (sqlite3_open(dbpath, &contactDB) == SQLITE_OK)
    {
        NSString *insertSQL = [NSString stringWithFormat:@"INSERT INTO GPSTable values(null, '%+.6f', '%.6f', '%.6f', '%@', '%@')", location.coordinate.latitude, location.coordinate.longitude, location.altitude, DateFormatter, EnterRegion];
        
        //NSString *insertSQL = [NSString stringWithFormat:@"INSERT INTO GPSTable values(null, '%.6f', '%.6f', '%.6f', '%@', '%@')", 15.12598, 524.12368, 15.6665, @"20160208T26:15:15", @"YES"];
        
        const char *insert_stmt = [insertSQL UTF8String];
        
        sqlite3_prepare_v2(contactDB, insert_stmt, -1, &statement, NULL);
        if (sqlite3_step(statement) == SQLITE_DONE)
        {
            NSLog(@"Success to add contact");
        } else {
            NSLog(@"Failed to add contact");
        }
        sqlite3_finalize(statement);
        sqlite3_close(contactDB);
    }
}
-(void)MakeJSON_Make{
    JSONArray = [[NSMutableArray alloc] init];
    if (sqlite3_open([dataBasePath UTF8String], &contactDB) == SQLITE_OK) {
        NSString *sql_makeJOSN = [NSString stringWithFormat:@"SELECT * FROM GPSTable"];
        sqlite3_stmt *statement;
        
        if (sqlite3_prepare_v2(contactDB, [sql_makeJOSN UTF8String], -1, &statement, NULL)==SQLITE_OK) {
            if (sqlite3_step(statement) == SQLITE_ROW) {
                
                while (sqlite3_step(statement) == SQLITE_ROW) {
                    
                    //NSLog(@"{timestamp: '%s', source: \"iOS\", gps: {latitude: %s, longitude: %s, altitude: %s, timerInterval %s}, region: {\"identifier\": \"%s\"}}", sqlite3_column_text(statement, 4),  sqlite3_column_text(statement, 1), sqlite3_column_text(statement, 2), sqlite3_column_text(statement, 3), sqlite3_column_text(statement, 4), sqlite3_column_text(statement, 5), sqlite3_column_text(statement, 6));
                    
                    
                    JSONString = [NSString stringWithFormat:@"{timestamp: '%s', source: \"iOS\", gps: {latitude: %s, longitude: %s, altitude: %s, timerInterval %s}, region: {\"identifier\": \"%s\"}},", sqlite3_column_text(statement, 4),  sqlite3_column_text(statement, 1), sqlite3_column_text(statement, 2), sqlite3_column_text(statement, 3), sqlite3_column_text(statement, 4), sqlite3_column_text(statement, 5), sqlite3_column_text(statement, 6)];
                    [JSONArray addObject:JSONString];
                    
                }
                sqlite3_finalize(statement);
                sqlite3_close(contactDB);
                [self postJSON];
                
            }
        }
    }
}

-(void)postJSON
{
    
    NSData *jsonData;
    NSString*jsonString;
    
    jsonData = [NSJSONSerialization dataWithJSONObject:JSONArray options:0 error:nil];
    jsonString = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    NSString *postLength = [NSString stringWithFormat:@"%lu", (unsigned long)[jsonData length]];
    
    
    // Be sure to properly escape your url string.
    //string for the URL request
    NSString *myUrlString = @"https://cpdev.dockrzone.com/api/v1/polls/vote/";
    //create a NSURL object from the string data
    NSURL *myUrl = [NSURL URLWithString:myUrlString];
    
    //create a mutable HTTP request
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:myUrl];
    
    
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:postLength forHTTPHeaderField:@"Content-Length"];
    [request setHTTPBody: jsonData];
    
    NSError *errorReturned = nil;
    NSURLResponse *theResponse =[[NSURLResponse alloc]init];
    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&theResponse error:&errorReturned];
    
    if (errorReturned) {
        // Handle error.
        NSLog(@"Handle error!");
    }
    else
    {
        NSString*query=[NSString stringWithFormat:@"DELETE FROM GPSTable"];
        if(sqlite3_exec(contactDB, [query UTF8String],NULL, NULL, NULL) == SQLITE_OK){
            NSLog(@"Delete Query Success");
            
        }
    }
    
}
@end
