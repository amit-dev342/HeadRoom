import type { Metadata,Viewport } from "next";import "./globals.css";
export const metadata:Metadata={title:"HeadRoom",description:"Make space. Move forward.",applicationName:"HeadRoom"};
export const viewport:Viewport={themeColor:"#111526",width:"device-width",initialScale:1,viewportFit:"cover"};
export default function RootLayout({children}:{children:React.ReactNode}){return <html lang="en"><body>{children}</body></html>}