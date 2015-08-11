/*
 * This file is a part of sample project which demonstrates how to use OpenCV
 * library with the XCode to write the iOS-based applications.
 * 
 * Written by Eugene Khvedchenya.
 * Distributed via GPL license. 
 * Support site: http://computer-vision-talks.com
 */

#import "iOSplusOpenCVViewController.hpp"
#import "iOSplusOpenCVAppDelegate.h"
#import <opencv2/imgproc/imgproc.hpp>
#import <opencv2/highgui/cap_ios.h>
#import "ObjRecog/controlOR.h"
#import "Tracking/trackingOBJ.h"
#import "ObjRecog/commonCvFunctions.h"
#include <iostream>

#include <sys/timeb.h>
typedef long long time_tt;

@interface iOSplusOpenCVViewController ()<CvVideoCameraDelegate>
{
    cv::Mat outputFrame;
    cv::Mat query_image;
    cvar::controlOR	ctrlOR;
    cvar::trackingOBJ* trckOBJ;
    bool inited;
    bool capture;
    bool track_f;
    int query_scale;
    
    int seq_id;
    int wait_seq_id;
}
@property (nonatomic, strong) CvVideoCamera* videoSource;
@property (strong, nonatomic) IBOutlet UIView *containerView;

@end

@implementation iOSplusOpenCVViewController
@synthesize mainButton, popoverController;

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    CGRect screenBounds = [[UIScreen mainScreen] bounds];
    CGRect viewFrame = screenBounds;
    
    // If this device has a retina display, scale the view bounds that will
    // be passed to QCAR; this allows it to calculate the size and position of
    // the viewport correctly when rendering the video background
    //if (YES == vapp.isRetinaDisplay) {
        //viewFrame.size.width *= 2.0;
        //viewFrame.size.height *= 2.0;
    //}

    
    //CGRect applicationFrame = [[UIScreen mainScreen] applicationFrame];
    self.containerView = [[UIView alloc] initWithFrame:viewFrame];
    self.videoSource = [[CvVideoCamera alloc] initWithParentView:self.containerView];
    self.videoSource.defaultAVCaptureDevicePosition = AVCaptureDevicePositionBack;
    //self.videoSource.defaultAVCaptureSessionPreset = AVCaptureSessionPreset640x480;
    //self.videoSource.defaultAVCaptureVideoOrientation = AVCaptureVideoOrientationPortrait;
    //self.videoSource.defaultFPS = 120;
    //self.videoSource.grayscaleMode = YES;
    //self.videoSource.imageHeight = 568;
    //self.videoSource.imageWidth = 320;
    self.videoSource.delegate = self;
    
    cv::Size frame_size = cv::Size(viewFrame.size.height, viewFrame.size.width);
    
    cv::FileStorage cvfs;
    NSString *path = [[NSBundle mainBundle] pathForResource:@"config" ofType:@"xml"];
    NSString *pathDir = [path stringByDeletingLastPathComponent];
    cvfs.open([path UTF8String],cv::FileStorage::READ);
    
    
    cv::FileNode fn;
    fn = cvfs["VisualWord"];
    std::string vwfile;
    fn["visualWord"] >> vwfile;
    vwfile = [pathDir UTF8String]  +("/"+ vwfile);
    
    std::string idxfile;
    fn["index"] >> idxfile;
    idxfile = [pathDir UTF8String] +("/"+ idxfile);
    if(idxfile.empty()){
        ctrlOR.loadVisualWords(vwfile);
    }
    else{
        ctrlOR.loadVisualWordsBinary(vwfile, idxfile);
    }
    
    std::string objectDB;
    fn["ObjectDB"] >> objectDB;
    objectDB = [pathDir UTF8String] +("/"+ objectDB);
    ctrlOR.loadObjectDB(objectDB);
    
    int max_query_size = 320;
    cvfs["max_query_size"] >> max_query_size;
    
    // クエリー用画像サイズを適切な大きさへ縮小して領域確保
    int frame_max_size;
    if(frame_size.width > frame_size.height){
        frame_max_size = frame_size.width;
    }
    else{
        frame_max_size = frame_size.height;
    }
    
    query_scale = 1;
    while((frame_max_size / query_scale) > max_query_size){
        query_scale*=2;
    }
    query_image.create(frame_size.height/query_scale, frame_size.width/query_scale, CV_8UC1);
    
    track_f = false;
    trckOBJ = cvar::trackingOBJ::create(cvar::trackingOBJ::TRACKER_KLT);
}

- (void)viewDidUnload
{
  [self setMainButton:nil];
  [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
  if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
      return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
  } else {
      return YES;
  }
}

#pragma mark - Selecting image from photo album

- (void) buttonDidTapped:(id)sender
{
    if(!inited){
        [self.view addSubview: self.containerView];
        self.view.alpha = 1.0;
        [self.view sendSubviewToBack:self.containerView];
        self.view = self.containerView;
        [self.videoSource start];
        inited = true;
    }else{
        //capture = true;
        //sample->setCapture(1);
    }


}

- (void)processImage:(cv::Mat&)image 
{
    // Do some OpenCV stuff with the image
    if(!inited)
        return;
    if(!track_f){
        cv::Mat frame;
        cv::cvtColor(image, frame, cv::COLOR_BGRA2GRAY);
        cv::resize(frame, query_image, query_image.size());
        
        timeb t;ftime(&t);
        time_tt start = 1000 * t.time + t.millitm;
        std::vector<cvar::resultInfo> recog_result = ctrlOR.queryImage(query_image);
        ftime(&t);
        time_tt end = 1000 * t.time + t.millitm;
        std::cout << "recognize cost time: " << end-start << " mill" << std::endl;
        
        if(!recog_result.empty()){
            cv::Mat pose_mat_scale = recog_result[0].pose_mat.clone();
            pose_mat_scale.row(0) *= query_scale;
            pose_mat_scale.row(1) *= query_scale;
            std::vector<cv::Point2f> scaled = cvar::scalePoints(recog_result[0].object_position, (double)3.0);
            trckOBJ->startTracking(frame, scaled);
            //track_f = viewMDL->setRecogId(recog_result[0].img_id, pose_mat_scale);
            track_f = true;
            seq_id = 0;
            wait_seq_id = 0;
            
        }
    
    }else{
        timeb t;ftime(&t);
        time_tt start = 1000 * t.time + t.millitm;
        track_f = trckOBJ->onTracking(image);
        ftime(&t);
        time_tt end = 1000 * t.time + t.millitm;
        
        
        seq_id++;
        if (track_f){
            std::cout << "tracking cost time: " << end-start << " mill" << std::endl;
        }
        
    }
    


}


@end
