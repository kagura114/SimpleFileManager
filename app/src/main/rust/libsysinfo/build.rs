fn main() {
    println!("cargo:rustc-link-arg=-z");
    println!("cargo:rustc-link-arg=max-page-size={}",1024*16);
}
