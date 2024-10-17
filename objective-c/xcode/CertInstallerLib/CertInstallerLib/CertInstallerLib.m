//
//  CertInstallerLib.m
//  CertInstallerLib
//
//  Created by Catas Wang on 2024-10-17.
//

#import <Foundation/Foundation.h>
#import "CertInstallerLib.h"

bool installCert(const char *certPath) {
    // Prepare the authorization reference
    AuthorizationRef authorizationRef;
    OSStatus status = AuthorizationCreate(NULL, kAuthorizationEmptyEnvironment, kAuthorizationFlagDefaults, &authorizationRef);
    
    if (status != errAuthorizationSuccess) {
        NSLog(@"Authorization creation failed.");
        return NO;
    }
    
    // Set up the authorization item for executing commands
    AuthorizationItem right = {kAuthorizationRightExecute, 0, NULL, 0};
    AuthorizationRights rights = {1, &right};
    
    // Set the authorization flags
    AuthorizationFlags flags = kAuthorizationFlagDefaults |
                               kAuthorizationFlagInteractionAllowed |
                               kAuthorizationFlagPreAuthorize |
                               kAuthorizationFlagExtendRights;
    
    // Request the privilege to run the command as root
    status = AuthorizationCopyRights(authorizationRef,
                                         &rights,
                                         kAuthorizationEmptyEnvironment,
                                         flags,
                                         NULL);
    
    if (status != errAuthorizationSuccess) {
        NSLog(@"Authorization failed. User might have denied the request.");
        AuthorizationFree(authorizationRef, kAuthorizationFlagDefaults);
        return NO;
    }
    
    // Define the command and arguments
    const char *command = "/usr/bin/security";
    char *args[] = {
        "add-trusted-cert",
        "-d",                // Add as a trusted root certificate
        "-r", "trustRoot",   // Set the trust level as root
        "-k", "/Library/Keychains/System.keychain",
        (char *)certPath,
        NULL
    };

    // Execute the command with privileges
    FILE *pipe = NULL;
    status = AuthorizationExecuteWithPrivileges(authorizationRef, command, kAuthorizationFlagDefaults, args, &pipe);
    if (status == errAuthorizationSuccess) {
        NSLog(@"Command executed successfully.");
    } else {
        NSLog(@"Failed to execute command with authorization.");
    }
    
    // Clean up
    AuthorizationFree(authorizationRef, kAuthorizationFlagDefaults);
    return status == errAuthorizationSuccess;
}

