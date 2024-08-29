#import "WindowBlur.h"
#import "Foundation/Foundation.h"
#import "AppKit/AppKit.h"

void setBlurWindow(void* windowptr, NSString *nsAppearanceName) {
    NSLog(@"Calling setBlurWindow with ptr: %ld, apperance: %@", (long)windowptr, nsAppearanceName);

    // Casting pointer into a NSWindow pointer
    NSWindow* win = (__bridge NSWindow*)(windowptr);
    NSLog(@"NSWindow: %@", win);
    
    
    dispatch_async(dispatch_get_main_queue(), ^{
        NSView* hostView = win.contentView;

        if (hostView != nil && hostView.subviews.count) {
            NSLog(@"Before NSVisualEffectView creation subviews.count = %lu", hostView.subviews.count);

            NSMutableArray *mutableSubviews = [hostView.subviews mutableCopy];
            for (NSView *subview in [mutableSubviews copy]) {
                // Perform some condition to determine if the subview should be removed
                if ([subview isKindOfClass:[NSVisualEffectView class]]) {
                    [subview removeFromSuperview];
                    [mutableSubviews removeObject:subview];
                }
            }

            // Now, mutableSubviews contains the modified array with items removed
            [hostView setSubviews:mutableSubviews];
            
            // iterate over sub views
            NSArray *subviews = [hostView subviews];
            NSUInteger count = [subviews count];

            for (NSUInteger i = 0; i < count; i++) {
                NSView *subview = [subviews objectAtIndex:i];
                
                // Perform operations on each subview
                NSLog(@"Current subview: %@", subview);
            }
            
            // set titlebar transparent
            win.titlebarAppearsTransparent = YES;

            // 设置窗口样式为无标题栏和全高内容
            win.styleMask |= NSWindowStyleMaskFullSizeContentView;

            NSView* jfxView = hostView.subviews[0];
            // if one already exists then remove it.
            
            NSVisualEffectView *vfxView = [[NSVisualEffectView alloc] initWithFrame:[win.contentView bounds]];
            [vfxView setAppearance:[NSAppearance appearanceNamed:nsAppearanceName]];
            [vfxView setBlendingMode:NSVisualEffectBlendingModeBehindWindow];
            [vfxView setMaterial:NSVisualEffectMaterialUnderWindowBackground];

            // make sure javafx layer is not opaque
            [vfxView setAutoresizingMask: (NSViewWidthSizable|NSViewHeightSizable)];
            [hostView addSubview: vfxView positioned: NSWindowBelow relativeTo: jfxView];
        }
    });
}
