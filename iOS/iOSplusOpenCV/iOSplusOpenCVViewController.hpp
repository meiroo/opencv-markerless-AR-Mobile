/*
 * This file is a part of sample project which demonstrates how to use OpenCV
 * library with the XCode to write the iOS-based applications.
 * 
 * Written by Eugene Khvedchenya.
 * Distributed via GPL license. 
 * Support site: http://computer-vision-talks.com
 */


#import <UIKit/UIKit.h>

@interface iOSplusOpenCVViewController : UIViewController<UIActionSheetDelegate>
{
  UIButton *mainButton;
  UIPopoverController *popoverController;
    
}

@property (strong, nonatomic) IBOutlet UIButton *mainButton;
@property (nonatomic, retain) UIPopoverController *popoverController;


- (IBAction)buttonDidTapped:(id)sender;

@end
