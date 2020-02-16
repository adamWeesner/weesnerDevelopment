package socialSecurity

import generics.GenericRouter

class SocialSecurityRouter :
    GenericRouter<SocialSecurity, SocialSecurityTable>(SocialSecurityService(), SocialSecurityResponse())