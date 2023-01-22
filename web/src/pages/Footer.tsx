export default function Footer() {
    return (
        <footer className="bg-gray-100 text-gray-600 body-font ">
            <div className="container text-center px-5 py-8 mx-auto items-center">
                <div className="flex flex-wrap justify-center">
                    <div className="w-full lg:w-1/2 px-4">
                        <h1 className="title font-medium text-gray-900 tracking-widest text-3xl mb-3">
                            hAshCreAte<br /><span className="text-blue-800 underline underline-offset-4 decoration-1 decoration-blue-800">SINCE 2023</span>
                        </h1>
                    </div>
                </div>
                <button className="flex mx-auto m-5 font-bold text-black bg-transparent border-2 border-black py-1 px-2 focus:outline-none hover:bg-black hover:text-white rounded text-lg">
                    <a href="/privacy">Your Privacy.</a>
                </button>
                <span className="tracking-widest uppercase bg-black text-white rounded p-1">Developed by <b>John Seong</b>, <b>Lyn Jeong</b>, and <b>Rayan Kaissi</b>.</span>
            </div>
        </footer>
    );
}